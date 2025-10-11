package com.example.loginandregistration.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.adapters.AdminItemsAdapter
import com.example.loginandregistration.admin.dialogs.ItemDetailsDialogFragment
import com.example.loginandregistration.admin.dialogs.StatusEditDialogFragment
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import com.example.loginandregistration.admin.models.ItemStatus
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar

class AdminItemsFragment : Fragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    private lateinit var itemsAdapter: AdminItemsAdapter
    
    // Views
    private lateinit var searchView: SearchView
    private lateinit var statusChipGroup: ChipGroup
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var rvItems: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnAdvancedFilter: com.google.android.material.floatingactionbutton.FloatingActionButton
    
    private var currentStatusFilter: ItemStatus? = null
    private var currentCategoryFilter: String? = null
    private var currentSearchQuery: String = ""
    private var advancedFilterCriteria: com.example.loginandregistration.admin.dialogs.ItemFilterBottomSheet.FilterCriteria? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_items, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupSearchView()
        setupStatusChips()
        setupCategoryChips()
        setupSwipeRefresh()
        observeViewModel()
        
        // Load initial data
        viewModel.loadAllItemsWithStatus()
    }
    
    private fun initViews(view: View) {
        searchView = view.findViewById(R.id.searchView)
        statusChipGroup = view.findViewById(R.id.statusChipGroup)
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        rvItems = view.findViewById(R.id.rvItems)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        btnAdvancedFilter = view.findViewById(R.id.btnAdvancedFilter)
        
        btnAdvancedFilter.setOnClickListener {
            showAdvancedFilterBottomSheet()
        }
    }
    
    private fun setupRecyclerView() {
        itemsAdapter = AdminItemsAdapter { item, action ->
            when (action) {
                "view" -> {
                    // Show item details
                    showItemDetails(item)
                }
                "edit_status" -> {
                    // Show status edit dialog
                    showStatusEditDialog(item)
                }
                "delete" -> {
                    // Show delete confirmation
                    showDeleteConfirmation(item)
                }
            }
        }
        
        rvItems.apply {
            adapter = itemsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                applyFilters()
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                applyFilters()
                return true
            }
        })
    }
    
    private fun setupStatusChips() {
        statusChipGroup.removeAllViews()
        
        // Add "All" chip
        val allChip = Chip(context).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                if (isChecked) {
                    currentStatusFilter = null
                    updateStatusChipSelection(null)
                    applyFilters()
                }
            }
        }
        statusChipGroup.addView(allChip)
        
        // Add status filter chips
        ItemStatus.values().forEach { status ->
            val chip = Chip(context).apply {
                text = getStatusDisplayName(status)
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        currentStatusFilter = status
                        updateStatusChipSelection(status)
                        applyFilters()
                    }
                }
            }
            statusChipGroup.addView(chip)
        }
    }
    
    private fun setupCategoryChips() {
        categoryChipGroup.removeAllViews()
        
        // Add "All Categories" chip
        val allChip = Chip(context).apply {
            text = "All Categories"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                if (isChecked) {
                    currentCategoryFilter = null
                    updateCategoryChipSelection(null)
                    applyFilters()
                }
            }
        }
        categoryChipGroup.addView(allChip)
        
        // Add common category chips
        val categories = listOf("Electronics", "Clothing", "Books", "Accessories", "Documents", "Keys", "Other")
        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        currentCategoryFilter = category
                        updateCategoryChipSelection(category)
                        applyFilters()
                    }
                }
            }
            categoryChipGroup.addView(chip)
        }
    }
    
    private fun updateStatusChipSelection(selectedStatus: ItemStatus?) {
        for (i in 0 until statusChipGroup.childCount) {
            val chip = statusChipGroup.getChildAt(i) as Chip
            if (selectedStatus == null) {
                chip.isChecked = chip.text.toString() == "All"
            } else {
                chip.isChecked = chip.text.toString() == getStatusDisplayName(selectedStatus)
            }
        }
    }
    
    private fun updateCategoryChipSelection(selectedCategory: String?) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as Chip
            if (selectedCategory == null) {
                chip.isChecked = chip.text.toString() == "All Categories"
            } else {
                chip.isChecked = chip.text.toString() == selectedCategory
            }
        }
    }
    
    private fun getStatusDisplayName(status: ItemStatus): String {
        return when (status) {
            ItemStatus.ACTIVE -> "Active"
            ItemStatus.REQUESTED -> "Requested"
            ItemStatus.RETURNED -> "Returned"
            ItemStatus.DONATION_PENDING -> "Donation Pending"
            ItemStatus.DONATION_READY -> "Donation Ready"
            ItemStatus.DONATED -> "Donated"
        }
    }
    
    private fun applyFilters() {
        val filters = mutableMapOf<String, String>()
        
        currentStatusFilter?.let {
            filters["status"] = it.name
        }
        
        currentCategoryFilter?.let {
            filters["category"] = it
        }
        
        if (currentSearchQuery.isNotEmpty()) {
            viewModel.searchItemsEnhanced(currentSearchQuery, filters)
        } else {
            viewModel.loadAllItemsWithStatus()
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.loadAllItemsWithStatus()
        }
    }
    
    private fun observeViewModel() {
        // Observe all items with status
        viewModel.allItemsWithStatus.observe(viewLifecycleOwner) { items ->
            var filteredItems = items
            
            // Apply status filter
            currentStatusFilter?.let { status ->
                filteredItems = filteredItems.filter { it.status == status }
            }
            
            // Apply category filter
            currentCategoryFilter?.let { category ->
                filteredItems = filteredItems.filter { it.category == category }
            }
            
            // Apply search filter
            if (currentSearchQuery.isNotEmpty()) {
                val query = currentSearchQuery.lowercase()
                filteredItems = filteredItems.filter {
                    it.name.lowercase().contains(query) ||
                    it.description.lowercase().contains(query) ||
                    it.location.lowercase().contains(query) ||
                    it.userEmail.lowercase().contains(query)
                }
            }
            
            // Apply advanced filters if set
            advancedFilterCriteria?.let { criteria ->
                // Date range filter
                criteria.startDate?.let { startDate ->
                    filteredItems = filteredItems.filter { 
                        it.timestamp.toDate().time >= startDate 
                    }
                }
                
                criteria.endDate?.let { endDate ->
                    filteredItems = filteredItems.filter { 
                        it.timestamp.toDate().time <= endDate 
                    }
                }
                
                // Location filter
                criteria.location?.let { location ->
                    val locationQuery = location.lowercase()
                    filteredItems = filteredItems.filter { 
                        it.location.lowercase().contains(locationQuery) 
                    }
                }
                
                // Reporter filter
                criteria.reporter?.let { reporter ->
                    val reporterQuery = reporter.lowercase()
                    filteredItems = filteredItems.filter { 
                        it.userEmail.lowercase().contains(reporterQuery) 
                    }
                }
            }
            
            itemsAdapter.submitList(filteredItems)
            swipeRefresh.isRefreshing = false
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show()
            }
        }
        
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showItemDetails(item: com.example.loginandregistration.LostFoundItem) {
        // Navigate to ItemDetailsFragment (will be implemented in task 10.2)
        // For now, show a simple dialog
        Snackbar.make(requireView(), "Item details: ${item.name}", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showStatusEditDialog(item: com.example.loginandregistration.LostFoundItem) {
        // Will be implemented in task 10.4
        Snackbar.make(requireView(), "Status edit for: ${item.name}", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showDeleteConfirmation(item: com.example.loginandregistration.LostFoundItem) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(item.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAdvancedFilterBottomSheet() {
        val bottomSheet = com.example.loginandregistration.admin.dialogs.ItemFilterBottomSheet.newInstance { criteria ->
            advancedFilterCriteria = criteria
            applyAdvancedFilters(criteria)
        }
        bottomSheet.show(parentFragmentManager, "ItemFilterBottomSheet")
    }
    
    private fun applyAdvancedFilters(criteria: com.example.loginandregistration.admin.dialogs.ItemFilterBottomSheet.FilterCriteria) {
        // Update search query
        currentSearchQuery = criteria.searchQuery
        
        // Update status filter
        currentStatusFilter = criteria.status
        updateStatusChipSelection(criteria.status)
        
        // Update category filter
        currentCategoryFilter = criteria.category
        updateCategoryChipSelection(criteria.category)
        
        // Apply all filters
        applyFilters()
    }
}