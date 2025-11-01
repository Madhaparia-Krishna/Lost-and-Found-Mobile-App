package com.example.loginandregistration

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2 adapter for browse tabs
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
class BrowseViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    companion object {
        const val TAB_COUNT = 5
        const val TAB_LOST = 0
        const val TAB_FOUND = 1
        const val TAB_RETURNED = 2
        const val TAB_MY_REQUESTS = 3
        const val TAB_ALL = 4
    }
    
    override fun getItemCount(): Int = TAB_COUNT
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TAB_LOST -> BrowseTabFragment.newInstance(BrowseTabFragment.TabFilterType.LOST)
            TAB_FOUND -> BrowseTabFragment.newInstance(BrowseTabFragment.TabFilterType.FOUND)
            TAB_RETURNED -> BrowseTabFragment.newInstance(BrowseTabFragment.TabFilterType.RETURNED)
            TAB_MY_REQUESTS -> MyRequestsTabFragment.newInstance()
            TAB_ALL -> BrowseTabFragment.newInstance(BrowseTabFragment.TabFilterType.ALL)
            else -> BrowseTabFragment.newInstance(BrowseTabFragment.TabFilterType.LOST)
        }
    }
    
    fun getTabTitle(position: Int): String {
        return when (position) {
            TAB_LOST -> "Lost Items"
            TAB_FOUND -> "Found Items"
            TAB_RETURNED -> "Returned"
            TAB_MY_REQUESTS -> "My Requests"
            TAB_ALL -> "All Items"
            else -> ""
        }
    }
}
