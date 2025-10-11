package com.example.loginandregistration.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.adapters.DonationQueueAdapter
import com.example.loginandregistration.admin.dialogs.DonationDetailsDialog
import com.example.loginandregistration.admin.dialogs.MarkReadyForDonationDialog
import com.example.loginandregistration.admin.dialogs.MarkAsDonatedDialog
import com.example.loginandregistration.admin.models.DonationItem
import com.example.loginandregistration.admin.models.DonationStatus
import com.example.loginandregistration.admin.models.DonationFilter
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

/**
 * Fragment for managing donation queue and workflow
 * Requirements: 3.2, 8.3
 * Task: 11.1
 */
class AdminDonationsFragment : Fragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    private lateinit var donationAdapter: DonationQueueAdapter
    
    // Views
    private lateinit var tabLayout: TabLayout
    private lateinit var rvDonations: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var ageRangeChipGroup: ChipGroup
    private lateinit var locationChipGroup: ChipGroup
    
    // Current filter state
    private var currentFilter = DonationFilter()
    private var currentTab = DonationStatus.PENDING
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_donations, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupTabs()
        setupRecyclerView()
        setupFilters()
        setupSwipeRefresh()
        observeViewModel()
        
        // Load initial data
        viewModel.loadDonationQueue()
    }
    
    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        rvDonations = view.findViewById(R.id.rvDonations)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup)
        ageRangeChipGroup = view.findViewById(R.id.ageRangeChipGroup)
        locationChipGroup = view.findViewById(R.id.locationChipGroup)
    }
    
    /**
     * Setup tab navigation for donation statuses
     * Requirements: 3.2
     */
    private fun setupTabs() {
        // Add tabs for each donation status
        tabLayout.addTab(tabLayout.newTab().setText("Pending"))
        tabLayout.addTab(tabLayout.newTab().setText("Ready"))
        tabLayout.addTab(tabLayout.newTab().setText("Donated"))
        
        // Handle tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = DonationStatus.PENDING
                        currentFilter = currentFilter.withStatus(DonationStatus.PENDING)
                    }
                    1 -> {
                        currentTab = DonationStatus.READY
                        currentFilter = currentFilter.withStatus(DonationStatus.READY)
                    }
                    2 -> {
                        currentTab = DonationStatus.DONATED
                        currentFilter = currentFilter.withStatus(DonationStatus.DONATED)
                    }
                }
                applyFilters()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    /**
     * Setup RecyclerView with adapter
     * Requirements: 3.2
     */
    private fun setupRecyclerView() {
        donationAdapter = DonationQueueAdapter { item, action ->
            when (action) {
                "view_details" -> showDonationDetails(item)
                "mark_ready" -> showMarkReadyDialog(item)
                "mark_donated" -> showMarkDonatedDialog(item)
            }
        }
        
        rvDonations.apply {
            adapter = donationAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    /**
     * Setup filter chips
     * Requirements: 3.9
     */
    private fun setupFilters() {
        setupCategoryFilters()
        setupAgeRangeFilters()
        setupLocationFilters()
    }
    
    private fun setupCategoryFilters() {
        categoryChipGroup.removeAllViews()
        
        // Add "All Categories" chip
        val allChip = Chip(context).apply {
            text = "All Categories"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                if (isChecked) {
                    currentFilter = currentFilter.withCategory(null)
                    updateCategoryChipSelection(null)
                    applyFilters()
                }
            }
        }
        categoryChipGroup.addView(allChip)
        
        // Add category filter chips
        val categories = listOf("Electronics", "Clothing", "Books", "Accessories", "Documents", "Keys", "Other")
        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        currentFilter = currentFilter.withCategory(category)
                        updateCategoryChipSelection(category)
                        applyFilters()
                    }
                }
            }
            categoryChipGroup.addView(chip)
        }
    }
    
    private fun setupAgeRangeFilters() {
        ageRangeChipGroup.removeAllViews()
        
        // Add "All Ages" chip
        val allChip = Chip(context).apply {
            text = "All Ages"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                if (isChecked) {
                    currentFilter = currentFilter.withAgeRange(null, null)
                    updateAgeRangeChipSelection(null)
                    applyFilters()
                }
            }
        }
        ageRangeChipGroup.addView(allChip)
        
        // Add age range filter chips
        val ageRanges = listOf(
            "1+ years" to Pair(365L, null),
            "2+ years" to Pair(730L, null),
            "3+ years" to Pair(1095L, null)
        )
        
        ageRanges.forEach { (label, range) ->
            val chip = Chip(context).apply {
                text = label
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        currentFilter = currentFilter.withAgeRange(range.first, range.second)
                        updateAgeRangeChipSelection(label)
                        applyFilters()
                    }
                }
            }
            ageRangeChipGroup.addView(chip)
        }
    }
    
    private fun setupLocationFilters() {
        locationChipGroup.removeAllViews()
        
        // Add "All Locations" chip
        val allChip = Chip(context).apply {
            text = "All Locations"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                if (isChecked) {
                    currentFilter = currentFilter.withLocation(null)
                    updateLocationChipSelection(null)
                    applyFilters()
                }
            }
        }
        locationChipGroup.addView(allChip)
        
        // Add common location filter chips
        val locations = listOf("Library", "Cafeteria", "Gym", "Classroom", "Parking", "Other")
        locations.forEach { location ->
            val chip = Chip(context).apply {
                text = location
                isCheckable = true
                isChecked = false
                setOnClickListener {
                    if (isChecked) {
                        currentFilter = currentFilter.withLocation(location)
                        updateLocationChipSelection(location)
                        applyFilters()
                    }
                }
            }
            locationChipGroup.addView(chip)
        }
    }
    
    private fun updateCategoryChipSelection(selectedCategory: String?) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as Chip
            chip.isChecked = if (selectedCategory == null) {
                chip.text.toString() == "All Categories"
            } else {
                chip.text.toString() == selectedCategory
            }
        }
    }
    
    private fun updateAgeRangeChipSelection(selectedRange: String?) {
        for (i in 0 until ageRangeChipGroup.childCount) {
            val chip = ageRangeChipGroup.getChildAt(i) as Chip
            chip.isChecked = if (selectedRange == null) {
                chip.text.toString() == "All Ages"
            } else {
                chip.text.toString() == selectedRange
            }
        }
    }
    
    private fun updateLocationChipSelection(selectedLocation: String?) {
        for (i in 0 until locationChipGroup.childCount) {
            val chip = locationChipGroup.getChildAt(i) as Chip
            chip.isChecked = if (selectedLocation == null) {
                chip.text.toString() == "All Locations"
            } else {
                chip.text.toString() == selectedLocation
            }
        }
    }
    
    /**
     * Apply current filters to donation queue
     * Requirements: 3.9
     */
    private fun applyFilters() {
        val donations = viewModel.donationQueue.value ?: emptyList()
        val filteredDonations = donations.filter { currentFilter.matches(it) }
        donationAdapter.submitList(filteredDonations)
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDonationQueue()
        }
    }
    
    /**
     * Observe ViewModel data
     * Requirements: 3.2, 8.3
     */
    private fun observeViewModel() {
        // Observe donation queue
        viewModel.donationQueue.observe(viewLifecycleOwner) { donations ->
            applyFilters()
            swipeRefresh.isRefreshing = false
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
        }
        
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Show donation details dialog
     * Requirements: 3.3
     */
    private fun showDonationDetails(item: DonationItem) {
        val dialog = DonationDetailsDialog.newInstance(item)
        dialog.show(parentFragmentManager, "DonationDetailsDialog")
    }
    
    /**
     * Show mark ready for donation dialog
     * Requirements: 3.4
     */
    private fun showMarkReadyDialog(item: DonationItem) {
        val dialog = MarkReadyForDonationDialog.newInstance(item) {
            viewModel.markItemReadyForDonation(item.itemId)
        }
        dialog.show(parentFragmentManager, "MarkReadyForDonationDialog")
    }
    
    /**
     * Show mark as donated dialog
     * Requirements: 3.5
     */
    private fun showMarkDonatedDialog(item: DonationItem) {
        val dialog = MarkAsDonatedDialog.newInstance(item) { recipient, value ->
            viewModel.markItemAsDonated(item.itemId, recipient, value)
        }
        dialog.show(parentFragmentManager, "MarkAsDonatedDialog")
    }
    
    companion object {
        fun newInstance() = AdminDonationsFragment()
    }
}
