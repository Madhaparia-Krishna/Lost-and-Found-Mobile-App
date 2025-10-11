package com.example.loginandregistration.admin.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.models.AdminUser
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Dialog for editing user details
 * Requirements: 1.6
 */
class EditUserDialog : DialogFragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    
    private lateinit var tilDisplayName: TextInputLayout
    private lateinit var etDisplayName: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    
    private var user: AdminUser? = null
    
    companion object {
        private const val ARG_USER_UID = "user_uid"
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"
        private const val ARG_USER_PHOTO = "user_photo"
        
        fun newInstance(user: AdminUser): EditUserDialog {
            val dialog = EditUserDialog()
            val args = Bundle()
            args.putString(ARG_USER_UID, user.uid)
            args.putString(ARG_USER_NAME, user.displayName)
            args.putString(ARG_USER_EMAIL, user.email)
            args.putString(ARG_USER_PHOTO, user.photoUrl)
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_user, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        populateFields()
        setupClickListeners()
    }
    
    override fun onStart() {
        super.onStart()
        // Make dialog full width
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun initViews(view: View) {
        tilDisplayName = view.findViewById(R.id.tilDisplayName)
        etDisplayName = view.findViewById(R.id.etDisplayName)
        tilEmail = view.findViewById(R.id.tilEmail)
        etEmail = view.findViewById(R.id.etEmail)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)
    }
    
    private fun populateFields() {
        etDisplayName.setText(arguments?.getString(ARG_USER_NAME) ?: "")
        etEmail.setText(arguments?.getString(ARG_USER_EMAIL) ?: "")
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnSave.setOnClickListener {
            saveUserDetails()
        }
    }
    
    private fun saveUserDetails() {
        val displayName = etDisplayName.text?.toString()?.trim() ?: ""
        
        // Validate display name
        if (displayName.isBlank()) {
            tilDisplayName.error = "Display name cannot be empty"
            return
        }
        
        if (displayName.length < 2) {
            tilDisplayName.error = "Display name must be at least 2 characters"
            return
        }
        
        if (displayName.length > 50) {
            tilDisplayName.error = "Display name must be less than 50 characters"
            return
        }
        
        // Clear errors
        tilDisplayName.error = null
        
        // Get user ID
        val userId = arguments?.getString(ARG_USER_UID) ?: return
        
        // Prepare updates map
        val updates = mutableMapOf<String, Any>()
        
        // Only add displayName if it changed
        val originalName = arguments?.getString(ARG_USER_NAME) ?: ""
        if (displayName != originalName) {
            updates["displayName"] = displayName
        }
        
        // Check if there are any changes
        if (updates.isEmpty()) {
            view?.let { v ->
                Snackbar.make(v, "No changes to save", Snackbar.LENGTH_SHORT).show()
            }
            dismiss()
            return
        }
        
        // Update user through ViewModel
        viewModel.updateUserDetailsEnhanced(userId, updates)
        
        // Show success message
        view?.let { v ->
            Snackbar.make(v, "User details updated successfully", Snackbar.LENGTH_SHORT).show()
        }
        
        // Dismiss dialog
        dismiss()
    }
}
