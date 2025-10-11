package com.example.loginandregistration.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
 * Dialog for blocking a user with reason input
 * Requirements: 1.3, 1.8
 */
class BlockUserDialog : DialogFragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    
    private lateinit var tvUserInfo: TextView
    private lateinit var tilBlockReason: TextInputLayout
    private lateinit var etBlockReason: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBlock: MaterialButton
    
    private var user: AdminUser? = null
    
    companion object {
        private const val ARG_USER_UID = "user_uid"
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_EMAIL = "user_email"
        
        fun newInstance(user: AdminUser): BlockUserDialog {
            val dialog = BlockUserDialog()
            val args = Bundle()
            args.putString(ARG_USER_UID, user.uid)
            args.putString(ARG_USER_NAME, user.displayName)
            args.putString(ARG_USER_EMAIL, user.email)
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_block_user, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupUserInfo()
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
        tvUserInfo = view.findViewById(R.id.tvUserInfo)
        tilBlockReason = view.findViewById(R.id.tilBlockReason)
        etBlockReason = view.findViewById(R.id.etBlockReason)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnBlock = view.findViewById(R.id.btnBlock)
    }
    
    private fun setupUserInfo() {
        val userName = arguments?.getString(ARG_USER_NAME) ?: "Unknown User"
        val userEmail = arguments?.getString(ARG_USER_EMAIL) ?: ""
        
        tvUserInfo.text = "Are you sure you want to block $userName ($userEmail)?"
    }
    
    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnBlock.setOnClickListener {
            blockUser()
        }
    }
    
    private fun blockUser() {
        val reason = etBlockReason.text?.toString()?.trim() ?: ""
        
        // Validate reason
        if (reason.isBlank()) {
            tilBlockReason.error = "Please provide a reason for blocking this user"
            return
        }
        
        if (reason.length < 10) {
            tilBlockReason.error = "Reason must be at least 10 characters"
            return
        }
        
        // Clear error
        tilBlockReason.error = null
        
        // Get user ID
        val userId = arguments?.getString(ARG_USER_UID) ?: return
        
        // Block user through ViewModel
        viewModel.blockUser(userId, reason)
        
        // Show success message
        view?.let { v ->
            Snackbar.make(v, "User blocked successfully", Snackbar.LENGTH_SHORT).show()
        }
        
        // Dismiss dialog
        dismiss()
    }
}
