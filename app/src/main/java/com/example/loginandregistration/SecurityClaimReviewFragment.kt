package com.example.loginandregistration

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.utils.ClaimManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment for security officers to review and manage claim requests.
 * Requirements: 5.5, 5.6
 */
class SecurityClaimReviewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var claimRequestAdapter: ClaimRequestAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val claimRequestList = mutableListOf<ClaimRequest>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_security_claim_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_claim_requests)
        emptyStateLayout = view.findViewById(R.id.layout_empty_state)
        
        recyclerView.layoutManager = LinearLayoutManager(context)

        claimRequestAdapter = ClaimRequestAdapter(
            onApproveClicked = { claimRequest -> approveClaim(claimRequest) },
            onRejectClicked = { claimRequest -> rejectClaim(claimRequest) }
        )
        recyclerView.adapter = claimRequestAdapter

        fetchClaimRequests()
    }

    /**
     * Fetches claim requests from Firestore using Flow
     */
    private fun fetchClaimRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            getClaimRequestsFlow()
                .flowOn(Dispatchers.IO)
                .collect { claimRequests ->
                    withContext(Dispatchers.Main) {
                        claimRequestList.clear()
                        claimRequestList.addAll(claimRequests)
                        claimRequestAdapter.submitList(claimRequests)
                        
                        // Show empty state if no claim requests
                        if (claimRequests.isEmpty()) {
                            recyclerView.visibility = View.GONE
                            emptyStateLayout.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            emptyStateLayout.visibility = View.GONE
                        }
                    }
                }
        }
    }

    /**
     * Creates a Flow that listens to claim requests collection
     */
    private fun getClaimRequestsFlow() = callbackFlow {
        val listener = db.collection("claimRequests")
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to load claim requests: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val claimRequests = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(ClaimRequest::class.java)
                    }
                    trySend(claimRequests)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Approves a claim request using ClaimManager
     * Requirements: 5.5, 5.6
     */
    private fun approveClaim(claimRequest: ClaimRequest) {
        if (claimRequest.requestId.isEmpty()) {
            Toast.makeText(context, "Cannot approve claim: Missing ID", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Claim")
            .setMessage("Are you sure you want to approve this claim request? The item will be marked as 'Returned'.")
            .setPositiveButton("Approve") { dialog, _ ->
                performApproveClaim(claimRequest, currentUser.uid)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Performs the claim approval using ClaimManager
     */
    private fun performApproveClaim(claimRequest: ClaimRequest, userId: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = ClaimManager.approveClaim(
                    requestId = claimRequest.requestId,
                    reviewedByUserId = userId
                )

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            "Claim approved successfully. Item marked as returned.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to approve claim: ${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to approve claim: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Rejects a claim request with optional notes dialog
     * Requirements: 5.5, 5.6
     */
    private fun rejectClaim(claimRequest: ClaimRequest) {
        if (claimRequest.requestId.isEmpty()) {
            Toast.makeText(context, "Cannot reject claim: Missing ID", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        showRejectNotesDialog(claimRequest, currentUser.uid)
    }

    /**
     * Shows a dialog to enter rejection notes
     */
    private fun showRejectNotesDialog(claimRequest: ClaimRequest, userId: String) {
        val editText = EditText(requireContext()).apply {
            hint = "Reason for rejection (optional)"
            minLines = 3
            maxLines = 5
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Reject Claim")
            .setMessage("Are you sure you want to reject this claim request?")
            .setView(editText)
            .setPositiveButton("Reject") { dialog, _ ->
                val notes = editText.text.toString().trim()
                performRejectClaim(claimRequest, userId, notes)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Performs the claim rejection using ClaimManager
     */
    private fun performRejectClaim(claimRequest: ClaimRequest, userId: String, notes: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = ClaimManager.rejectClaim(
                    requestId = claimRequest.requestId,
                    reviewedByUserId = userId,
                    reviewNotes = notes
                )

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(
                            context,
                            "Claim rejected successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to reject claim: ${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to reject claim: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
