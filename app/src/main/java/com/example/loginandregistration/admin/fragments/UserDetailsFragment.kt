package com.example.loginandregistration.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.loginandregistration.R
import com.example.loginandregistration.admin.models.AdminUser
import com.example.loginandregistration.admin.models.UserRole
import com.example.loginandregistration.admin.viewmodel.AdminDashboardViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment to display detailed user information
 * Requirements: 1.2, 8.1
 */
class UserDetailsFragment : Fragment() {
    
    private val viewModel: AdminDashboardViewModel by activityViewModels()
    
    // Views
    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var chipRole: Chip
    private lateinit var chipStatus: Chip
    private lateinit var tvItemsReported: TextView
    private lateinit var tvItemsFound: TextView
    private lateinit var tvItemsClaimed: TextView
    private lateinit var tvAccountCreated: TextView
    private lateinit var tvLastLogin: TextView
    private lateinit var tvUserId: TextView
    private lateinit var btnEditUser: MaterialButton
    private lateinit var btnChangeRole: MaterialButton
    private lateinit var btnBlockUnblock: MaterialButton
    
    private var currentUser: AdminUser? = null
    
    companion object {
        private const val ARG_USER_ID = "user_id"
        
        fun newInstance(userId: String): UserDetailsFragment {
            val fragment = UserDetailsFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        observeViewModel()
        
        // Load user details
        arguments?.getString(ARG_USER_ID)?.let { userId ->
            loadUserDetails(userId)
        }
    }
    
    private fun initViews(view: View) {
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        chipRole = view.findViewById(R.id.chipRole)
        chipStatus = view.findViewById(R.id.chipStatus)
        tvItemsReported = view.findViewById(R.id.tvItemsReported)
        tvItemsFound = view.findViewById(R.id.tvItemsFound)
        tvItemsClaimed = view.findViewById(R.id.tvItemsClaimed)
        tvAccountCreated = view.findViewById(R.id.tvAccountCreated)
        tvLastLogin = view.findViewById(R.id.tvLastLogin)
        tvUserId = view.findViewById(R.id.tvUserId)
        btnEditUser = view.findViewById(R.id.btnEditUser)
        btnChangeRole = view.findViewById(R.id.btnChangeRole)
        btnBlockUnblock = view.findViewById(R.id.btnBlockUnblock)
    }
    
    private fun setupClickListeners() {
        btnEditUser.setOnClickListener {
            currentUser?.let { user ->
                showEditUserDialog(user)
            }
        }
        
        btnChangeRole.setOnClickListener {
            currentUser?.let { user ->
                showRoleChangeDialog(user)
            }
        }
        
        btnBlockUnblock.setOnClickListener {
            currentUser?.let { user ->
                if (user.isBlocked) {
                    showUnblockConfirmation(user)
                } else {
                    showBlockUserDialog(user)
                }
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.allUsers.observe(viewLifecycleOwner) { users ->
            // Update current user if it's in the list
            currentUser?.let { current ->
                users.find { it.uid == current.uid }?.let { updated ->
                    currentUser = updated
                    displayUserDetails(updated)
                }
            }
        }
        
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                view?.let { v ->
                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                view?.let { v ->
                    Snackbar.make(v, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun loadUserDetails(userId: String) {
        // Find user from the all users list
        viewModel.allUsers.value?.find { it.uid == userId }?.let { user ->
            currentUser = user
            displayUserDetails(user)
        }
    }
    
    private fun displayUserDetails(user: AdminUser) {
        // Basic info
        tvUserName.text = user.displayName.ifEmpty { "Unknown User" }
        tvUserEmail.text = user.email
        tvUserId.text = user.uid
        
        // Load avatar
        if (user.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(ivUserAvatar)
        } else {
            ivUserAvatar.setImageResource(R.drawable.ic_person_placeholder)
        }
        
        // Role chip
        chipRole.text = user.role.name
        chipRole.setChipBackgroundColorResource(getRoleColor(user.role))
        
        // Status chip
        chipStatus.text = if (user.isBlocked) "Blocked" else "Active"
        chipStatus.setChipBackgroundColorResource(
            if (user.isBlocked) R.color.status_lost else R.color.status_found
        )
        
        // Statistics
        tvItemsReported.text = user.itemsReported.toString()
        tvItemsFound.text = user.itemsFound.toString()
        tvItemsClaimed.text = user.itemsClaimed.toString()
        
        // Account information
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvAccountCreated.text = sdf.format(Date(user.createdAt))
        
        if (user.lastLoginAt > 0) {
            tvLastLogin.text = getTimeAgo(user.lastLoginAt)
        } else {
            tvLastLogin.text = "Never"
        }
        
        // Update block/unblock button
        btnBlockUnblock.text = if (user.isBlocked) "Unblock User" else "Block User"
        btnBlockUnblock.setIconResource(
            if (user.isBlocked) R.drawable.ic_check else R.drawable.ic_block
        )
    }
    
    private fun getRoleColor(role: UserRole): Int {
        return when (role) {
            UserRole.ADMIN -> R.color.status_lost
            UserRole.MODERATOR -> R.color.status_pending
            UserRole.SECURITY -> R.color.status_pending
            UserRole.STUDENT -> R.color.status_default
            UserRole.USER -> R.color.status_default
        }
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            else -> "Just now"
        }
    }
    
    private fun showEditUserDialog(user: AdminUser) {
        // TODO: This will be implemented in task 9.4
        // val dialog = EditUserDialog.newInstance(user)
        // dialog.show(parentFragmentManager, "EditUserDialog")
        Snackbar.make(requireView(), "Edit user dialog - Coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showRoleChangeDialog(user: AdminUser) {
        // TODO: This will be implemented in task 9.5
        // val dialog = RoleChangeDialog.newInstance(user)
        // dialog.show(parentFragmentManager, "RoleChangeDialog")
        
        // Temporary implementation - show simple dialog
        val roles = arrayOf("USER", "MODERATOR", "ADMIN")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Change User Role")
            .setItems(roles) { _, which ->
                val newRole = when (which) {
                    0 -> UserRole.USER
                    1 -> UserRole.MODERATOR
                    2 -> UserRole.ADMIN
                    else -> UserRole.USER
                }
                viewModel.updateUserRole(user.uid, newRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showBlockUserDialog(user: AdminUser) {
        // TODO: This will be implemented in task 9.3
        // val dialog = BlockUserDialog.newInstance(user)
        // dialog.show(parentFragmentManager, "BlockUserDialog")
        
        // Temporary implementation - show simple dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Block User")
            .setMessage("Are you sure you want to block ${user.displayName}?")
            .setPositiveButton("Block") { _, _ ->
                viewModel.blockUser(user.uid, "Blocked by admin")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUnblockConfirmation(user: AdminUser) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Unblock User")
            .setMessage("Are you sure you want to unblock ${user.displayName}?")
            .setPositiveButton("Unblock") { _, _ ->
                viewModel.unblockUser(user.uid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
