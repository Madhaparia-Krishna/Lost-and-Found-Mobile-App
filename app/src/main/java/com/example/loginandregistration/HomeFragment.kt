package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemsAdapter
    private lateinit var etSearch: EditText
    private lateinit var cardReportLost: CardView
    private lateinit var cardReportFound: CardView
    
    private val db = FirebaseFirestore.getInstance()
    
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
        db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(6)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LostFoundItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                // Add sample data if no items exist
                if (items.isEmpty()) {
                    addSampleData()
                } else {
                    adapter.updateItems(items)
                }
            }
    }
    
    private fun addSampleData() {
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
        
        sampleItems.forEach { item ->
            db.collection("items").add(item)
        }
    }
}