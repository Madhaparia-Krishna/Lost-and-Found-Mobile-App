package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(
    private val reportList: List<LostFoundItem>,
    private val onApproveClicked: (LostFoundItem) -> Unit,
    private val onRejectClicked: (LostFoundItem) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tv_item_name)
        val itemDescription: TextView = itemView.findViewById(R.id.tv_item_description)
        val reportedBy: TextView = itemView.findViewById(R.id.tv_reported_by)
        val status: TextView = itemView.findViewById(R.id.tv_status)
        val approveButton: Button = itemView.findViewById(R.id.btn_approve)
        val rejectButton: Button = itemView.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        holder.itemName.text = report.name
        holder.itemDescription.text = report.description
        holder.reportedBy.text = "By: ${report.userEmail}" // Shows user email
        holder.status.text = report.status

        // Change status color for visibility
        when (report.status) {
            "Approved" -> holder.status.setTextColor(Color.parseColor("#4CAF50")) // Green
            "Rejected" -> holder.status.setTextColor(Color.parseColor("#F44336")) // Red
            else -> holder.status.setTextColor(Color.parseColor("#FF9800")) // Orange
        }

        // Set click listeners for the buttons
        holder.approveButton.setOnClickListener {
            onApproveClicked(report)
        }
        holder.rejectButton.setOnClickListener {
            onRejectClicked(report)
        }
    }

    override fun getItemCount() = reportList.size
}
