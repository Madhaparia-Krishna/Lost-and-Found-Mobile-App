package com.example.loginandregistration

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.utils.ApprovalManager
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()
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

        recyclerView = view.findViewById(R.id.security_reports_recyclerview)
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

    /**
     * Approves a report using ApprovalManager
     * Requirements: 4.3, 4.6
     */
    private fun approveReport(report: LostFoundItem) {
        if (report.id.isEmpty()) {
            Toast.makeText(context, "Cannot approve report: Missing ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Use lifecycleScope to launch coroutine on IO dispatcher
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Use ApprovalManager to approve the item
                val result = ApprovalManager.approveItem(
                    itemId = report.id,
                    approvedByUserId = currentUser.uid
                )
                
                // Show success or error message on Main thread
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, "Item approved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context, 
                            "Failed to approve item: ${result.exceptionOrNull()?.message}", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // The addSnapshotListener will automatically refresh the list
            } catch (e: Exception) {
                // Show error message on Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to approve item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Rejects a report with optional notes dialog
     * Requirements: 4.4, 4.6
     */
    private fun rejectReport(report: LostFoundItem) {
        if (report.id.isEmpty()) {
            Toast.makeText(context, "Cannot reject report: Missing ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show dialog to get rejection notes
        showRejectNotesDialog(report, currentUser.uid)
    }
    
    /**
     * Shows a dialog to enter rejection notes
     */
    private fun showRejectNotesDialog(report: LostFoundItem, userId: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null)
        
        val editText = EditText(requireContext()).apply {
            hint = "Reason for rejection (optional)"
            minLines = 3
            maxLines = 5
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Reject Item")
            .setMessage("Are you sure you want to reject this item?")
            .setView(editText)
            .setPositiveButton("Reject") { dialog, _ ->
                val notes = editText.text.toString().trim()
                performReject(report, userId, notes)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Performs the rejection using ApprovalManager
     */
    private fun performReject(report: LostFoundItem, userId: String, notes: String) {
        // Use lifecycleScope to launch coroutine on IO dispatcher
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Use ApprovalManager to reject the item
                val result = ApprovalManager.rejectItem(
                    itemId = report.id,
                    rejectedByUserId = userId,
                    notes = notes
                )
                
                // Show success or error message on Main thread
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, "Item rejected successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context, 
                            "Failed to reject item: ${result.exceptionOrNull()?.message}", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // The addSnapshotListener will automatically refresh the list
            } catch (e: Exception) {
                // Show error message on Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to reject item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
