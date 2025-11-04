package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Browse fragment with tabbed interface for different item categories
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 7.5
 */
class BrowseFragment : Fragment() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var searchView: SearchView
    private lateinit var pagerAdapter: BrowseViewPagerAdapter
    
    private var currentSearchQuery: String = ""
    
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
        
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)
        searchView = view.findViewById(R.id.search_view)
        
        setupViewPager()
        setupSearchView()
    }
    
    private fun setupViewPager() {
        // Create adapter with four tabs
        // Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
        pagerAdapter = BrowseViewPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // Optimize ViewPager2 for better performance
        viewPager.offscreenPageLimit = 1 // Preload adjacent pages only
        viewPager.isUserInputEnabled = true // Enable swipe gestures
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = pagerAdapter.getTabTitle(position)
        }.attach()
        
        // Listen for tab changes to apply search query to new tab
        // Requirement: 7.5
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Apply current search query to newly selected tab
                applySearchToCurrentTab(currentSearchQuery)
            }
        })
    }
    
    private fun setupSearchView() {
        // Implement search filtering with real-time updates
        // Requirements: 7.2, 7.3, 7.4, 7.5
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Apply search when user submits
                val searchQuery = query ?: ""
                currentSearchQuery = searchQuery
                applySearchToCurrentTab(searchQuery)
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                // Apply search in real-time as user types
                // Requirement: 7.2
                val searchQuery = newText ?: ""
                currentSearchQuery = searchQuery
                applySearchToCurrentTab(searchQuery)
                return true
            }
        })
        
        // Clear search when close button is clicked
        searchView.setOnCloseListener {
            currentSearchQuery = ""
            applySearchToCurrentTab("")
            false
        }
    }
    
    /**
     * Applies the search query to the currently visible tab fragment
     * Requirements: 7.3, 7.5
     */
    private fun applySearchToCurrentTab(query: String) {
        val currentPosition = viewPager.currentItem
        // ViewPager2 uses a specific tag format: f{containerId}{position}
        val fragmentTag = "f${viewPager.id}${currentPosition}"
        val fragment = childFragmentManager.findFragmentByTag(fragmentTag)
        
        if (fragment is SearchableFragment) {
            fragment.applySearchFilter(query)
        }
    }
}