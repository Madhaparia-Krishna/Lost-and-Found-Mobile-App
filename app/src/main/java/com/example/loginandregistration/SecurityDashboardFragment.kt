// --- ADD THIS BLOCK ---
package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


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
        // Use lifecycleScope to launch coroutine tied to fragment lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            // Collect reports from Flow on IO dispatcher, update UI on Main
            getReportsFlow()
                .flowOn(Dispatchers.IO)
                .collect { reports ->
                    // Update UI on Main thread
                    withContext(Dispatchers.Main) {
                        reportList.clear()
                        reportList.addAll(reports)
                        reportAdapter.notifyDataSetChanged()
                    }
                }
        }
    }
    
    private fun getReportsFlow() = callbackFlow {
        val listener = db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Show error on main thread
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load reports: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val reports = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(LostFoundItem::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    trySend(reports)
                }
            }
        
        // Remove listener when flow is cancelled
        awaitClose { listener.remove() }
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
        
        // Use lifecycleScope to launch coroutine on IO dispatcher
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Update the 'status' field of the specific document
                db.collection("items").document(report.id)
                    .update("status", newStatus)
                    .await()
                
                // Show success message on Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Report status updated to '$newStatus'", Toast.LENGTH_SHORT).show()
                }
                // The addSnapshotListener will automatically refresh the list
            } catch (e: Exception) {
                // Show error message on Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
