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
import com.example.loginandregistration.admin.adapters.AdminUsersAdapter
import com.example.loginandregistration.admin.models.AdminUser
import com.example.loginandregistration.admin.models.UserRole
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AdminUsersFragment : Fragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    private lateinit var usersAdapter: AdminUsersAdapter
    
    // Views
    private lateinit var searchView: SearchView
    private lateinit var rvUsers: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var chipAllUsers: Chip
    private lateinit var chipActiveUsers: Chip
    private lateinit var chipBlockedUsers: Chip
    private lateinit var chipAdminRole: Chip
    private lateinit var chipModeratorRole: Chip
    private lateinit var chipUserRole: Chip
    
    // Filter state
    private var currentSearchQuery: String = ""
    private var statusFilter: StatusFilter = StatusFilter.ALL
    private var roleFilters: MutableSet<UserRole> = mutableSetOf()
    private var allUsersList: List<AdminUser> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_users, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupSearchView()
        setupFilterChips()
        setupSwipeRefresh()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        searchView = view.findViewById(R.id.searchView)
        rvUsers = view.findViewById(R.id.rvUsers)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters)
        chipAllUsers = view.findViewById(R.id.chipAllUsers)
        chipActiveUsers = view.findViewById(R.id.chipActiveUsers)
        chipBlockedUsers = view.findViewById(R.id.chipBlockedUsers)
        chipAdminRole = view.findViewById(R.id.chipAdminRole)
        chipModeratorRole = view.findViewById(R.id.chipModeratorRole)
        chipUserRole = view.findViewById(R.id.chipUserRole)
    }
    
    private fun setupRecyclerView() {
        usersAdapter = AdminUsersAdapter { user, action ->
            when (action) {
                "block" -> {
                    viewModel.updateUserBlockStatus(user.uid, !user.isBlocked)
                }
                "change_role" -> {
                    showRoleChangeDialog(user)
                }
                "view_details" -> {
                    showUserDetails(user)
                }
            }
        }
        
        rvUsers.apply {
            adapter = usersAdapter
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
    
    private fun setupFilterChips() {
        // Status filter chips
        chipAllUsers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                statusFilter = StatusFilter.ALL
                chipActiveUsers.isChecked = false
                chipBlockedUsers.isChecked = false
                applyFilters()
            }
        }
        
        chipActiveUsers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                statusFilter = StatusFilter.ACTIVE
                chipAllUsers.isChecked = false
                chipBlockedUsers.isChecked = false
                applyFilters()
            }
        }
        
        chipBlockedUsers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                statusFilter = StatusFilter.BLOCKED
                chipAllUsers.isChecked = false
                chipActiveUsers.isChecked = false
                applyFilters()
            }
        }
        
        // Role filter chips
        chipAdminRole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                roleFilters.add(UserRole.ADMIN)
            } else {
                roleFilters.remove(UserRole.ADMIN)
            }
            applyFilters()
        }
        
        chipModeratorRole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                roleFilters.add(UserRole.MODERATOR)
            } else {
                roleFilters.remove(UserRole.MODERATOR)
            }
            applyFilters()
        }
        
        chipUserRole.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                roleFilters.add(UserRole.USER)
            } else {
                roleFilters.remove(UserRole.USER)
            }
            applyFilters()
        }
    }
    
    private fun applyFilters() {
        var filteredList = allUsersList
        
        // Apply status filter
        filteredList = when (statusFilter) {
            StatusFilter.ALL -> filteredList
            StatusFilter.ACTIVE -> filteredList.filter { !it.isBlocked }
            StatusFilter.BLOCKED -> filteredList.filter { it.isBlocked }
        }
        
        // Apply role filters
        if (roleFilters.isNotEmpty()) {
            filteredList = filteredList.filter { user ->
                roleFilters.contains(user.role)
            }
        }
        
        // Apply search query
        if (currentSearchQuery.isNotBlank()) {
            filteredList = filteredList.filter { user ->
                user.email.contains(currentSearchQuery, ignoreCase = true) ||
                user.displayName.contains(currentSearchQuery, ignoreCase = true)
            }
        }
        
        usersAdapter.submitList(filteredList)
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
            swipeRefresh.isRefreshing = false
        }
    }
    
    private fun observeViewModel() {
        viewModel.allUsers.observe(viewLifecycleOwner) { users ->
            allUsersList = users
            applyFilters()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Show error message
            }
        }
    }
    
    private fun showRoleChangeDialog(user: AdminUser) {
        val roles = UserRole.values()
        val roleNames = roles.map { it.name }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change User Role")
            .setItems(roleNames) { _, which ->
                val selectedRole = roles[which]
                viewModel.updateUserRole(user.uid, selectedRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUserDetails(user: AdminUser) {
        // Navigate to UserDetailsFragment
        val fragment = UserDetailsFragment.newInstance(user.uid)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    enum class StatusFilter {
        ALL, ACTIVE, BLOCKED
    }
}