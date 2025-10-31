package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private lateinit var progressBar: android.widget.ProgressBar
    private val db = FirebaseFirestore.getInstance()
    
    companion object {
        private const val TAG = "BrowseFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view_items)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = ItemsAdapter()
        recyclerView.adapter = adapter
        
        loadItems()
    }
    
    private fun loadItems() {
        // Use lifecycleScope to launch coroutine tied to fragment lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Show loading indicator
                showLoading()
                
                // Collect items from Flow on IO dispatcher, update UI on Main
                getItemsFlow()
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        // Handle errors in the flow
                        hideLoading()
                        handleError(e)
                    }
                    .collect { items ->
                        // Update UI on Main thread
                        withContext(Dispatchers.Main) {
                            hideLoading()
                            adapter.submitList(items)
                        }
                    }
            } catch (e: Exception) {
                hideLoading()
                handleError(e)
            }
        }
    }
    
    private fun getItemsFlow() = callbackFlow {
        val listener = db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Log the error and close flow
                    Log.e(TAG, "Firestore snapshot listener error: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }
                
                // Use mapNotNull to skip problematic items during deserialization
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing item ${doc.id}: ${e.message}", e)
                        null  // Skip problematic items
                    }
                } ?: emptyList()
                
                // Send items to flow
                trySend(items)
            }
        
        // Remove listener when flow is cancelled
        awaitClose { listener.remove() }
    }
    
    private suspend fun handleError(e: Throwable) {
        Log.e(TAG, "Error loading items: ${e.message}", e)
        
        withContext(Dispatchers.Main) {
            hideLoading()
            
            val message = when (e) {
                is FirebaseFirestoreException -> {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                            "Access denied. Please check your permissions and try again."
                        FirebaseFirestoreException.Code.UNAVAILABLE -> 
                            "Network error. Please check your connection and try again."
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                            "Authentication required. Please sign in again."
                        else -> 
                            "Error loading items: ${e.message}"
                    }
                }
                else -> "Failed to load items. Please try again."
            }
            
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}