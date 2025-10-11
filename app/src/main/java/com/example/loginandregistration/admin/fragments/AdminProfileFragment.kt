package com.example.loginandregistration.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.loginandregistration.Login
import com.example.loginandregistration.R
import com.google.firebase.auth.FirebaseAuth

class AdminProfileFragment : Fragment() {
    
    private lateinit var tvAdminEmail: TextView
    private lateinit var tvAdminName: TextView
    private lateinit var tvAdminRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnRefreshData: Button
    private lateinit var btnCreateTestData: Button
    
    private val auth = FirebaseAuth.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupUserInfo()
        setupClickListeners()
    }
    
    private fun initViews(view: View) {
        tvAdminEmail = view.findViewById(R.id.tvAdminEmail)
        tvAdminName = view.findViewById(R.id.tvAdminName)
        tvAdminRole = view.findViewById(R.id.tvAdminRole)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnRefreshData = view.findViewById(R.id.btnRefreshData)
        btnCreateTestData = view.findViewById(R.id.btnCreateTestData)
    }
    
    private fun setupUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvAdminEmail.text = currentUser.email
            tvAdminName.text = currentUser.displayName ?: "Admin User"
            tvAdminRole.text = "Administrator"
        }
    }
    
    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
        
        btnRefreshData.setOnClickListener {
            // Refresh dashboard data
            (activity as? com.example.loginandregistration.admin.AdminDashboardActivity)?.refreshData()
        }
        
        btnCreateTestData.setOnClickListener {
            // Create test data
            (activity as? com.example.loginandregistration.admin.AdminDashboardActivity)?.createTestData()
        }
    }
}