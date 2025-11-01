package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.utils.SearchManager
import com.example.loginandregistration.utils.UserRoleManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), SearchableFragment {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLoadMore: Button
    private lateinit var fabReport: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var searchView: SearchView
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Pagination state variables
    private var lastVisibleItem: DocumentSnapshot? = null
    private var isLoadingMore = false
    
    // Search state variables
    private var allItems: List<LostFoundItem> = emptyList()
    private var currentSearchQuery: String = ""
    
    companion object {
        private const val TAG = "HomeFragment"
        private const val ITEMS_PER_PAGE = 20
    }
    
    /**
     * Show dialog to select report type (Lost or Found)
     * Requirements: 4.2, 4.3, 4.4
     */
    private fun showReportTypeDialog() {
        val dialog = ReportTypeDialog.newInstance { reportType ->
            navigateToReportWithType(reportType)
        }
        dialog.show(parentFragmentManager, "ReportTypeDialog")
    }
    
    /**
     * Navigate to ReportFragment with pre-selected type
     * Requirements: 3.1, 3.2, 4.4, 4.5
     */
    private fun navigateToReportWithType(reportType: String) {
        val bundle = Bundle().apply {
            putString(ReportFragment.ARG_REPORT_TYPE, reportType)
        }
        
        val reportFragment = ReportFragment().apply {
            arguments = bundle
        }
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, reportFragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recycler_view_items)
        progressBar = view.findViewById(R.id.progress_bar)
        btnLoadMore = view.findViewById(R.id.btn_load_more)
        fabReport = view.findViewById(R.id.fab_report)
        searchView = view.findViewById(R.id.search_view)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Get current user email for role-based visibility
        // Requirement: 10.5
        val currentUserId = auth.currentUser?.uid ?: ""
        val userEmail = auth.currentUser?.email ?: ""
        
        adapter = ItemsAdapter(
            currentUserId = currentUserId,
            userEmail = userEmail
        )
        recyclerView.adapter = adapter
        
        // Set up Load More button
        btnLoadMore.setOnClickListener {
            loadMoreItems()
        }
        
        // Set up FAB to show report type dialog
        fabReport.setOnClickListener {
            showReportTypeDialog()
        }
        
        // Set up search functionality
        setupSearchView()
        
        loadRecentItems()
    }
    
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchQuery = query ?: ""
                currentSearchQuery = searchQuery
                applySearchFilter(searchQuery)
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchQuery = newText ?: ""
                currentSearchQuery = searchQuery
                applySearchFilter(searchQuery)
                return true
            }
        })
        
        searchView.setOnCloseListener {
            currentSearchQuery = ""
            applySearchFilter("")
            false
        }
    }
    
    private fun loadRecentItems() {
        // Use lifecycleScope to launch coroutine tied to fragment lifecycle
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Show loading indicator on Main thread
                withContext(Dispatchers.Main) {
                    showLoading()
                }
                
                val currentUser = auth.currentUser
                val userEmail = currentUser?.email ?: ""
                val isSecurity = UserRoleManager.canViewSensitiveInfo(userEmail)
                
                // Fetch data from Firestore on background thread
                val querySnapshot = db.collection("items")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(ITEMS_PER_PAGE.toLong())
                    .get()
                    .await()
                
                // Store last visible item for pagination
                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisibleItem = querySnapshot.documents.last()
                }
                
                // Map documents to LostFoundItem objects, skipping problematic items
                val fetchedItems = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing item ${doc.id}: ${e.message}", e)
                        null  // Skip problematic items
                    }
                }
                
                // Filter items based on user role
                // Requirements: 4.5, 4.6
                val filteredItems = if (isSecurity) {
                    // Security/admin users see all items
                    fetchedItems
                } else {
                    // Regular users see only approved items
                    fetchedItems.filter { it.status == "Approved" }
                }
                
                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    hideLoading()
                    
                    if (filteredItems.isEmpty()) {
                        // Add sample data if no items exist
                        addSampleData()
                    } else {
                        // Store all items for search filtering
                        allItems = filteredItems
                        
                        // Apply current search filter
                        val displayItems = if (currentSearchQuery.isBlank()) {
                            filteredItems
                        } else {
                            SearchManager.filterItems(filteredItems, currentSearchQuery)
                        }
                        
                        adapter.submitList(displayItems)
                        // Show Load More button if we got a full page
                        showLoadMoreButton(filteredItems.size == ITEMS_PER_PAGE)
                    }
                }
                
            } catch (e: FirebaseFirestoreException) {
                // Handle Firestore-specific errors
                handleFirestoreError(e)
            } catch (e: Exception) {
                // Handle generic errors
                handleGenericError(e)
            }
        }
    }
    
    private fun loadMoreItems() {
        // Prevent multiple simultaneous loads
        if (isLoadingMore || lastVisibleItem == null) {
            return
        }
        
        isLoadingMore = true
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                val userEmail = currentUser?.email ?: ""
                val isSecurity = UserRoleManager.canViewSensitiveInfo(userEmail)
                
                // Fetch next page from Firestore
                val querySnapshot = db.collection("items")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleItem!!)
                    .limit(ITEMS_PER_PAGE.toLong())
                    .get()
                    .await()
                
                // Update last visible item for next pagination
                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisibleItem = querySnapshot.documents.last()
                }
                
                // Map documents to LostFoundItem objects
                val newItems = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing item ${doc.id}: ${e.message}", e)
                        null
                    }
                }
                
                // Filter items based on user role
                val filteredNewItems = if (isSecurity) {
                    newItems
                } else {
                    newItems.filter { it.status == "Approved" }
                }
                
                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    isLoadingMore = false
                    
                    if (filteredNewItems.isNotEmpty()) {
                        // Update all items list
                        val updatedAllItems = allItems.toMutableList()
                        updatedAllItems.addAll(filteredNewItems)
                        allItems = updatedAllItems
                        
                        // Apply current search filter
                        val displayItems = if (currentSearchQuery.isBlank()) {
                            allItems
                        } else {
                            SearchManager.filterItems(allItems, currentSearchQuery)
                        }
                        
                        adapter.submitList(displayItems)
                        
                        // Show/hide Load More button based on items count
                        showLoadMoreButton(filteredNewItems.size == ITEMS_PER_PAGE)
                    } else {
                        // No more items to load
                        showLoadMoreButton(false)
                    }
                }
                
            } catch (e: FirebaseFirestoreException) {
                handleFirestoreError(e)
                withContext(Dispatchers.Main) {
                    isLoadingMore = false
                }
            } catch (e: Exception) {
                handleGenericError(e)
                withContext(Dispatchers.Main) {
                    isLoadingMore = false
                }
            }
        }
    }
    
    private suspend fun handleFirestoreError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Firestore error: ${e.code} - ${e.message}", e)
        
        withContext(Dispatchers.Main) {
            hideLoading()
            
            val message = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                    "Access denied. Please check your permissions and try again."
                FirebaseFirestoreException.Code.UNAVAILABLE -> 
                    "Network error. Please check your connection and try again."
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                    "Authentication required. Please sign in again."
                FirebaseFirestoreException.Code.NOT_FOUND -> 
                    "Data not found. Please try again later."
                else -> 
                    "Error loading items: ${e.message}"
            }
            
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
    
    private suspend fun handleGenericError(e: Exception) {
        Log.e(TAG, "Error loading items: ${e.message}", e)
        
        withContext(Dispatchers.Main) {
            hideLoading()
            Toast.makeText(
                requireContext(),
                "Failed to load items. Please try again.",
                Toast.LENGTH_LONG
            ).show()
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
    
    private fun showLoadMoreButton(show: Boolean) {
        btnLoadMore.visibility = if (show && !isLoadingMore) View.VISIBLE else View.GONE
    }
    
    private fun addSampleData() {
        // Run on IO dispatcher to avoid blocking main thread
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Show loading indicator
                withContext(Dispatchers.Main) {
                    showLoading()
                }
                
                val sampleItems = listOf(
                    LostFoundItem(
                        name = "iPhone 13",
                        description = "Black iPhone 13, has a blue case",
                        location = "Library",
                        contactInfo = "john@example.com",
                        isLost = true,
                        userId = "sample1",
                        userEmail = "john@example.com",
                        timestamp = com.google.firebase.Timestamp.now()
                    ),
                    LostFoundItem(
                        name = "Calculator",
                        description = "Scientific calculator, Casio brand",
                        location = "Math Building",
                        contactInfo = "mary@example.com",
                        isLost = false,
                        userId = "sample2",
                        userEmail = "mary@example.com",
                        timestamp = com.google.firebase.Timestamp.now()
                    )
                )
                
                // Add items to Firestore
                sampleItems.forEach { item ->
                    try {
                        db.collection("items").add(item).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding sample item: ${e.message}", e)
                    }
                }
                
                // Reload items after adding sample data
                loadRecentItems()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample data: ${e.message}", e)
                
                withContext(Dispatchers.Main) {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        "Failed to add sample data.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /**
     * Applies search filter to displayed items
     * Implementation of SearchableFragment interface
     */
    override fun applySearchFilter(query: String) {
        currentSearchQuery = query
        
        // Apply search filter using SearchManager
        val filteredItems = if (query.isBlank()) {
            allItems
        } else {
            SearchManager.filterItems(allItems, query)
        }
        
        // Update adapter with filtered items
        adapter.submitList(filteredItems)
    }
}
