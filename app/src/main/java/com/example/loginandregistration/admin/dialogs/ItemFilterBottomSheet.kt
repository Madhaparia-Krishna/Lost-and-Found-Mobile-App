package com.example.loginandregistration.admin.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.models.ItemStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bottom sheet for advanced item filtering
 * Requirements: 2.7
 */
class ItemFilterBottomSheet : BottomSheetDialogFragment() {
    
    private lateinit var etSearchQuery: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var cgStatus: ChipGroup
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etReporter: TextInputEditText
    private lateinit var btnApplyFilters: MaterialButton
    private lateinit var btnClearFilters: MaterialButton
    
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    private var onFiltersApplied: ((FilterCriteria) -> Unit)? = null
    
    companion object {
        fun newInstance(onFiltersApplied: (FilterCriteria) -> Unit): ItemFilterBottomSheet {
            return ItemFilterBottomSheet().apply {
                this.onFiltersApplied = onFiltersApplied
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_item_filter, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupCategoryDropdown()
        setupStatusChips()
        setupDatePickers()
        setupButtons()
    }
    
    private fun initViews(view: View) {
        etSearchQuery = view.findViewById(R.id.etSearchQuery)
        actvCategory = view.findViewById(R.id.actvCategory)
        cgStatus = view.findViewById(R.id.cgStatus)
        etStartDate = view.findViewById(R.id.etStartDate)
        etEndDate = view.findViewById(R.id.etEndDate)
        etLocation = view.findViewById(R.id.etLocation)
        etReporter = view.findViewById(R.id.etReporter)
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters)
        btnClearFilters = view.findViewById(R.id.btnClearFilters)
    }
    
    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "All Categories",
            "Electronics",
            "Clothing",
            "Books",
            "Accessories",
            "Documents",
            "Keys",
            "Bags",
            "Jewelry",
            "Sports Equipment",
            "Other"
        )
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
        actvCategory.setText("All Categories", false)
    }
    
    private fun setupStatusChips() {
        cgStatus.removeAllViews()
        
        // Add "All" chip
        val allChip = Chip(context).apply {
            text = "All"
            isCheckable = true
            isChecked = true
        }
        cgStatus.addView(allChip)
        
        // Add status chips
        ItemStatus.values().forEach { status ->
            val chip = Chip(context).apply {
                text = getStatusDisplayName(status)
                isCheckable = true
                isChecked = false
            }
            cgStatus.addView(chip)
        }
    }
    
    private fun setupDatePickers() {
        etStartDate.setOnClickListener {
            showDatePicker { date ->
                startDateMillis = date
                etStartDate.setText(dateFormat.format(Date(date)))
            }
        }
        
        etEndDate.setOnClickListener {
            showDatePicker { date ->
                endDateMillis = date
                etEndDate.setText(dateFormat.format(Date(date)))
            }
        }
    }
    
    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun setupButtons() {
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }
        
        btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }
    
    private fun applyFilters() {
        val searchQuery = etSearchQuery.text.toString().trim()
        val category = actvCategory.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val reporter = etReporter.text.toString().trim()
        
        // Get selected status
        var selectedStatus: ItemStatus? = null
        for (i in 0 until cgStatus.childCount) {
            val chip = cgStatus.getChildAt(i) as Chip
            if (chip.isChecked && chip.text.toString() != "All") {
                selectedStatus = getStatusFromDisplayName(chip.text.toString())
                break
            }
        }
        
        val criteria = FilterCriteria(
            searchQuery = searchQuery,
            category = if (category == "All Categories") null else category,
            status = selectedStatus,
            startDate = if (startDateMillis > 0) startDateMillis else null,
            endDate = if (endDateMillis > 0) endDateMillis else null,
            location = location.ifEmpty { null },
            reporter = reporter.ifEmpty { null }
        )
        
        onFiltersApplied?.invoke(criteria)
        dismiss()
    }
    
    private fun clearFilters() {
        etSearchQuery.setText("")
        actvCategory.setText("All Categories", false)
        etStartDate.setText("")
        etEndDate.setText("")
        etLocation.setText("")
        etReporter.setText("")
        startDateMillis = 0
        endDateMillis = 0
        
        // Reset status chips
        for (i in 0 until cgStatus.childCount) {
            val chip = cgStatus.getChildAt(i) as Chip
            chip.isChecked = chip.text.toString() == "All"
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
    
    private fun getStatusFromDisplayName(displayName: String): ItemStatus? {
        return when (displayName) {
            "Active" -> ItemStatus.ACTIVE
            "Requested" -> ItemStatus.REQUESTED
            "Returned" -> ItemStatus.RETURNED
            "Donation Pending" -> ItemStatus.DONATION_PENDING
            "Donation Ready" -> ItemStatus.DONATION_READY
            "Donated" -> ItemStatus.DONATED
            else -> null
        }
    }
    
    data class FilterCriteria(
        val searchQuery: String = "",
        val category: String? = null,
        val status: ItemStatus? = null,
        val startDate: Long? = null,
        val endDate: Long? = null,
        val location: String? = null,
        val reporter: String? = null
    )
}
