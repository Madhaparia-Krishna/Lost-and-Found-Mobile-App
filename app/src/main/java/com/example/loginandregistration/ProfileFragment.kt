package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserName: TextView
    private lateinit var btnLogout: Button
    
    private val auth = FirebaseAuth.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        tvUserName = view.findViewById(R.id.tv_user_name)
        btnLogout = view.findViewById(R.id.btn_logout)
        
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUserEmail.text = currentUser.email
            tvUserName.text = currentUser.displayName ?: "User"
        }
        
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}