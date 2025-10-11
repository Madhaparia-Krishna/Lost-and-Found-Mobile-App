// --- ADD THIS BLOCK ---
package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class SecurityDashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private val db = FirebaseFirestore.getInstance()
    private val reportList = mutableListOf<LostFoundItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_security_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_security_reports)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // The adapter now needs callbacks for approve and reject actions
        reportAdapter = ReportAdapter(
            reportList,
            onApproveClicked = { report -> approveReport(report) },
            onRejectClicked = { report -> rejectReport(report) }
        )
        recyclerView.adapter = reportAdapter

        fetchReports()
    }

    private fun fetchReports() {
        // Fetch all items, ordered by timestamp so newest are first
        db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Failed to load reports: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    reportList.clear()
                    for (doc in snapshots.documents) {
                        val report = doc.toObject(LostFoundItem::class.java)
                        // It's crucial to also get the document ID for updates
                        if (report != null) {
                            report.id = doc.id
                            reportList.add(report)
                        }
                    }
                    reportAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun approveReport(report: LostFoundItem) {
        updateReportStatus(report, "Approved")
    }

    private fun rejectReport(report: LostFoundItem) {
        updateReportStatus(report, "Rejected")
    }

    private fun updateReportStatus(report: LostFoundItem, newStatus: String) {
        if (report.id.isEmpty()) {
            Toast.makeText(context, "Cannot update report: Missing ID", Toast.LENGTH_SHORT).show()
            return
        }
        // Update the 'status' field of the specific document
        db.collection("items").document(report.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "Report status updated to '$newStatus'", Toast.LENGTH_SHORT).show()
                // The addSnapshotListener will automatically refresh the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
