package com.example.loginandregistration.admin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.models.EnhancedAdminUser

/**
 * Adapter for displaying top contributors in user analytics
 * Requirements: 1.7
 */
class TopContributorsAdapter : ListAdapter<EnhancedAdminUser, TopContributorsAdapter.ContributorViewHolder>(ContributorDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_contributor, parent, false)
        return ContributorViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContributorViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
    
    class ContributorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvContributions: TextView = itemView.findViewById(R.id.tvContributions)
        
        fun bind(user: EnhancedAdminUser, rank: Int) {
            tvRank.text = "#$rank"
            tvName.text = user.displayName.ifEmpty { "Unknown User" }
            
            val totalContributions = user.itemsReported + user.itemsFound + user.itemsClaimed
            tvContributions.text = "$totalContributions items"
            
            // Load avatar
            if (user.photoUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            }
        }
    }
    
    class ContributorDiffCallback : DiffUtil.ItemCallback<EnhancedAdminUser>() {
        override fun areItemsTheSame(oldItem: EnhancedAdminUser, newItem: EnhancedAdminUser): Boolean {
            return oldItem.uid == newItem.uid
        }
        
        override fun areContentsTheSame(oldItem: EnhancedAdminUser, newItem: EnhancedAdminUser): Boolean {
            return oldItem == newItem
        }
    }
}
