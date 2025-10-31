package com.example.loginandregistration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ItemsAdapter : ListAdapter<LostFoundItem, ItemsAdapter.ItemViewHolder>(ItemDiffCallback()) {

    // It's more efficient to create the SimpleDateFormat instance once
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), dateFormat)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemIcon: ImageView = itemView.findViewById(R.id.iv_item_icon)
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemLocation: TextView = itemView.findViewById(R.id.tv_item_location)
        private val tvItemStatus: TextView = itemView.findViewById(R.id.tv_item_status)
        private val tvItemDate: TextView = itemView.findViewById(R.id.tv_item_date)

        // Pass the date formatter here to avoid creating it repeatedly in a loop
        fun bind(item: LostFoundItem, dateFormat: SimpleDateFormat) {
            tvItemName.text = item.name
            tvItemLocation.text = item.location
            tvItemStatus.text = if (item.isLost) "Lost" else "Found"

            // --- FIX START ---
            // Safely handle the nullable timestamp.
            // The 'let' block only executes if item.timestamp is not null.
            item.timestamp?.let { timestamp ->
                tvItemDate.text = dateFormat.format(timestamp.toDate())
            } ?: run {
                // This 'run' block executes if item.timestamp is null.
                tvItemDate.text = "Date not available"
            }
            // --- FIX END ---

            // Set status background color
            val context = itemView.context
            if (item.isLost) {
                tvItemStatus.setBackgroundColor(context.getColor(R.color.lost_tag))
            } else {
                tvItemStatus.setBackgroundColor(context.getColor(R.color.found_tag))
            }

            // Load image using Glide with placeholder and error handling
            // Handle nullable imageUrl properly
            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_item_default)
                    .centerCrop()
                    .into(ivItemIcon)
            } else {
                // Set default icon based on item type if no image URL
                ivItemIcon.setImageResource(
                    if (item.name.lowercase().contains("phone")) R.drawable.ic_phone
                    else R.drawable.ic_item_default
                )
            }
        }
    }
    
    class ItemDiffCallback : DiffUtil.ItemCallback<LostFoundItem>() {
        override fun areItemsTheSame(oldItem: LostFoundItem, newItem: LostFoundItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: LostFoundItem, newItem: LostFoundItem): Boolean {
            return oldItem == newItem
        }
    }
}
