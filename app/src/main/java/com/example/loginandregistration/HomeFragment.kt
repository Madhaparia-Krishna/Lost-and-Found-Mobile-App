package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private lateinit var etSearch: EditText
    private lateinit var cardReportLost: CardView
    private lateinit var cardReportFound: CardView
    private lateinit var progressBar: ProgressBar
    
    private val db = FirebaseFirestore.getInstance()
    
    companion object {
        private const val TAG = "HomeFragment"
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
        etSearch = view.findViewById(R.id.et_search)
        cardReportLost = view.findViewById(R.id.card_report_lost)
        cardReportFound = view.findViewById(R.id.card_report_found)
        progressBar = view.findViewById(R.id.progress_bar)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ItemsAdapter()
        recyclerView.adapter = adapter
        
        // Set click listeners for report cards
        cardReportLost.setOnClickListener {
            // Navigate to report fragment with lost pre-selected
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    ?.selectedItemId = R.id.nav_report
            }
        }
        
        cardReportFound.setOnClickListener {
            // Navigate to report fragment with found pre-selected
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    ?.selectedItemId = R.id.nav_report
            }
        }
        
        loadRecentItems()
    }
    
    private fun loadRecentItems() {
        // Use lifecycleScope to launch coroutine tied to fragment lifecycle
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Show loading indicator on Main thread
                withContext(Dispatchers.Main) {
                    showLoading()
                }
                
                // Fetch data from Firestore on background thread
                val querySnapshot = db.collection("items")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(6)
                    .get()
                    .await()
                
                // Map documents to LostFoundItem objects, skipping problematic items
                val items = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing item ${doc.id}: ${e.message}", e)
                        null  // Skip problematic items
                    }
                }
                
                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    hideLoading()
                    
                    if (items.isEmpty()) {
                        // Add sample data if no items exist
                        addSampleData()
                    } else {
                        adapter.submitList(items)
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
}
