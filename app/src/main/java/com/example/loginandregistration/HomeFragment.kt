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
        
        // Optimize RecyclerView for better performance
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true) // Items have fixed size
        recyclerView.setItemViewCacheSize(20) // Cache more items
        recyclerView.isNestedScrollingEnabled = true
        
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
                // Use default source which checks cache first, then server
                val querySnapshot = db.collection("items")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(ITEMS_PER_PAGE.toLong())
                    .get()
                    .await()
                
                val finalSnapshot = querySnapshot
                
                // Store last visible item for pagination
                if (finalSnapshot.documents.isNotEmpty()) {
                    lastVisibleItem = finalSnapshot.documents.last()
                }
                
                // Map documents to LostFoundItem objects, skipping problematic items
                val fetchedItems = finalSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null  // Skip problematic items silently
                    }
                }
                
                // Filter items based on user role
                val filteredItems = if (isSecurity) {
                    fetchedItems
                } else {
                    fetchedItems.filter { it.status == "Approved" }
                }
                
                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    hideLoading()
                    
                    if (filteredItems.isEmpty()) {
                        // Show empty state instead of adding sample data
                        adapter.submitList(emptyList())
                        showLoadMoreButton(false)
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
                // Handle Firestore-specific errors silently for better UX
                withContext(Dispatchers.Main) {
                    hideLoading()
                    adapter.submitList(emptyList())
                }
            } catch (e: Exception) {
                // Handle generic errors silently
                withContext(Dispatchers.Main) {
                    hideLoading()
                    adapter.submitList(emptyList())
                }
            }
        }
    }
    
    private fun loadMoreItems() {
        // Prevent multiple simultaneous loads
        if (isLoadingMore || lastVisibleItem == null) {
            return
        }
        
        isLoadingMore = true
        btnLoadMore.isEnabled = false
        
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
                    btnLoadMore.isEnabled = true
                    
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
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingMore = false
                    btnLoadMore.isEnabled = true
                }
            }
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
