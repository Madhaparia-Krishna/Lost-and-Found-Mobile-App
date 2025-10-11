package com.example.loginandregistration.admin.dialogs

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.models.EnhancedLostFoundItem
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Dialog for editing item details
 * Requirements: 2.4
 */
class EditItemDialog : DialogFragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    
    private lateinit var etItemName: TextInputEditText
    private lateinit var etItemDescription: TextInputEditText
    private lateinit var etItemLocation: TextInputEditText
    private lateinit var etItemContact: TextInputEditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var ivItemImage: ImageView
    private lateinit var btnChangeImage: MaterialButton
    
    private var currentItem: EnhancedLostFoundItem? = null
    private var selectedImageUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .centerCrop()
                .into(ivItemImage)
        }
    }
    
    companion object {
        private const val ARG_ITEM = "item"
        
        fun newInstance(item: EnhancedLostFoundItem): EditItemDialog {
            return EditItemDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = arguments?.getSerializable(ARG_ITEM) as? EnhancedLostFoundItem
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null)
        
        initViews(view)
        setupCategoryDropdown()
        populateFields()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Item")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveChanges()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    private fun initViews(view: View) {
        etItemName = view.findViewById(R.id.etItemName)
        etItemDescription = view.findViewById(R.id.etItemDescription)
        etItemLocation = view.findViewById(R.id.etItemLocation)
        etItemContact = view.findViewById(R.id.etItemContact)
        actvCategory = view.findViewById(R.id.actvCategory)
        ivItemImage = view.findViewById(R.id.ivItemImage)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)
        
        btnChangeImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }
    
    private fun setupCategoryDropdown() {
        val categories = arrayOf(
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
    }
    
    private fun populateFields() {
        currentItem?.let { item ->
            etItemName.setText(item.name)
            etItemDescription.setText(item.description)
            etItemLocation.setText(item.location)
            etItemContact.setText(item.contactInfo)
            actvCategory.setText(item.category, false)
            
            // Load current image
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(ivItemImage)
            }
        }
    }
    
    private fun saveChanges() {
        val item = currentItem ?: return
        
        // Validate inputs
        val name = etItemName.text.toString().trim()
        val description = etItemDescription.text.toString().trim()
        val location = etItemLocation.text.toString().trim()
        val contact = etItemContact.text.toString().trim()
        val category = actvCategory.text.toString().trim()
        
        if (name.isEmpty()) {
            Snackbar.make(requireView(), "Item name is required", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (description.isEmpty()) {
            Snackbar.make(requireView(), "Description is required", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (location.isEmpty()) {
            Snackbar.make(requireView(), "Location is required", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (category.isEmpty()) {
            Snackbar.make(requireView(), "Category is required", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // Create updates map
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "description" to description,
            "location" to location,
            "contactInfo" to contact,
            "category" to category,
            "lastModifiedAt" to System.currentTimeMillis()
        )
        
        // If image was changed, handle image upload
        selectedImageUri?.let { uri ->
            // For now, we'll just note that image upload would happen here
            // In a real implementation, you would upload to Firebase Storage
            // and get the download URL
            // updates["imageUrl"] = downloadUrl
        }
        
        // Update item
        viewModel.updateItemDetailsEnhanced(item.id, updates)
    }
}
