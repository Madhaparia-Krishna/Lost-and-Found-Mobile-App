package com.example.loginandregistration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ItemsAdapter : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {
    
    private var items = listOf<LostFoundItem>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun updateItems(newItems: List<LostFoundItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found, parent, false)
        return ItemViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemIcon: ImageView = itemView.findViewById(R.id.iv_item_icon)
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemLocation: TextView = itemView.findViewById(R.id.tv_item_location)
        private val tvItemStatus: TextView = itemView.findViewById(R.id.tv_item_status)
        private val tvItemDate: TextView = itemView.findViewById(R.id.tv_item_date)
        
        fun bind(item: LostFoundItem) {
            tvItemName.text = item.name
            tvItemLocation.text = item.location
            tvItemStatus.text = if (item.isLost) "Lost" else "Found"
            tvItemDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.timestamp.toDate())
            
            // Set status background color
            val context = itemView.context
            if (item.isLost) {
                tvItemStatus.setBackgroundColor(context.getColor(R.color.lost_tag))
            } else {
                tvItemStatus.setBackgroundColor(context.getColor(R.color.found_tag))
            }
            
            // Set default icon based on item type
            // You can enhance this to show actual images later
            ivItemIcon.setImageResource(
                if (item.name.lowercase().contains("phone")) R.drawable.ic_phone
                else R.drawable.ic_item_default
            )
        }
    }
}