package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.utils.SearchManager
import com.example.loginandregistration.utils.UserRoleManager
import com.google.firebase.auth.FirebaseAuth
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

/**
 * Fragment for displaying filtered items in browse tabs
 * Requirements: 6.2, 6.3, 6.4, 7.5
 */
class BrowseTabFragment : Fragment(), SearchableFragment {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyMessage: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var filterType: TabFilterType = TabFilterType.LOST
    private var allItems: List<LostFoundItem> = emptyList()
    private var currentSearchQuery: String = ""
    
    companion object {
        private const val TAG = "BrowseTabFragment"
        private const val ARG_FILTER_TYPE = "filter_type"
        
        fun newInstance(filterType: TabFilterType): BrowseTabFragment {
            return BrowseTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILTER_TYPE, filterType.name)
                }
            }
        }
    }
    
    enum class TabFilterType {
        LOST,    // Lost items with status "Approved"
        FOUND,   // Found items with status "Approved"
        RETURNED, // Items with status "Returned"
        ALL      // All items with status "Approved" or "Returned"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val filterTypeName = it.getString(ARG_FILTER_TYPE)
            filterType = TabFilterType.valueOf(filterTypeName ?: TabFilterType.LOST.name)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse_tab, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view_items)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyState = view.findViewById(R.id.empty_state)
        emptyMessage = view.findViewById(R.id.tv_empty_message)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Get current user ID and email for role-based visibility
        // Requirement: 10.5
        val currentUserId = auth.currentUser?.uid ?: ""
        val userEmail = auth.currentUser?.email ?: ""
        
        // Initialize adapter with claim click listener
        adapter = ItemsAdapter(
            currentUserId = currentUserId,
            userEmail = userEmail,
            onClaimClickListener = { item ->
                // Handle claim button click - will be implemented in task 6
                Toast.makeText(requireContext(), "Claim functionality coming soon", Toast.LENGTH_SHORT).show()
            },
            pendingClaimItemIds = emptySet() // Will be populated when claim system is implemented
        )
        recyclerView.adapter = adapter
        
        // Set empty message based on filter type
        emptyMessage.text = when (filterType) {
            TabFilterType.LOST -> "No lost items found"
            TabFilterType.FOUND -> "No found items available"
            TabFilterType.RETURNED -> "No returned items"
            TabFilterType.ALL -> "No items found"
        }
        
        loadItems()
    }
    
    private fun loadItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading()
                
                getItemsFlow()
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        hideLoading()
                        handleError(e)
                    }
                    .collect { items ->
                        withContext(Dispatchers.Main) {
                            hideLoading()
                            
                            // Store all items for search filtering
                            // Requirement: 7.5
                            allItems = items
                            
                            // Apply current search filter
                            val displayItems = if (currentSearchQuery.isBlank()) {
                                items
                            } else {
                                SearchManager.filterItems(items, currentSearchQuery)
                            }
                            
                            adapter.submitList(displayItems)
                            
                            // Show empty state if no items
                            if (displayItems.isEmpty()) {
                                showEmptyState()
                            } else {
                                hideEmptyState()
                            }
                        }
                    }
            } catch (e: Exception) {
                hideLoading()
                handleError(e)
            }
        }
    }
    
    private fun getItemsFlow() = callbackFlow {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: ""
        val isSecurity = UserRoleManager.canViewSensitiveInfo(userEmail)
        
        val listener = db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Firestore snapshot listener error: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }
                
                val allItems = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing item ${doc.id}: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                
                // Apply filter based on tab type
                // Requirements: 6.2, 6.3, 6.4
                val filteredItems = when (filterType) {
                    TabFilterType.LOST -> {
                        // Show approved lost items
                        allItems.filter { it.isLost && it.status == "Approved" }
                    }
                    TabFilterType.FOUND -> {
                        // Show approved found items
                        allItems.filter { !it.isLost && it.status == "Approved" }
                    }
                    TabFilterType.RETURNED -> {
                        // Show returned items (regardless of lost/found type)
                        allItems.filter { it.status == "Returned" }
                    }
                    TabFilterType.ALL -> {
                        // Show all approved or returned items regardless of isLost
                        allItems.filter { it.status == "Approved" || it.status == "Returned" }
                    }
                }
                
                trySend(filteredItems)
            }
        
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
                            "Access denied. Please check your permissions."
                        FirebaseFirestoreException.Code.UNAVAILABLE -> 
                            "Network error. Please check your connection."
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
        emptyState.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    /**
     * Applies search filter to displayed items
     * Requirements: 7.2, 7.3, 7.4, 7.5
     */
    override fun applySearchFilter(query: String) {
        currentSearchQuery = query
        
        // Apply search filter using SearchManager
        // Requirement: 7.2, 7.3, 7.4
        val filteredItems = if (query.isBlank()) {
            allItems
        } else {
            SearchManager.filterItems(allItems, query)
        }
        
        // Update adapter with filtered items
        adapter.submitList(filteredItems)
        
        // Update empty state
        if (filteredItems.isEmpty()) {
            // Show different message when search returns no results
            if (query.isNotBlank()) {
                emptyMessage.text = "No items match your search"
            } else {
                emptyMessage.text = when (filterType) {
                    TabFilterType.LOST -> "No lost items found"
                    TabFilterType.FOUND -> "No found items available"
                    TabFilterType.RETURNED -> "No returned items"
                    TabFilterType.ALL -> "No items found"
                }
            }
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }
}
