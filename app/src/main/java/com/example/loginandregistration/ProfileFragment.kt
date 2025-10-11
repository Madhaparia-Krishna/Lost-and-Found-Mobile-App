package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.loginandregistration.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    // In app/src/main/java/com/example/loginandregistration/ProfileFragment.kt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize Firebase Auth
        auth = Firebase.auth

        // --- NEW: Display the user's email ---
        displayUserInfo()

        // --- UPDATED: Set up the new password change logic ---
        binding.btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        // --- FIX: ADD THIS LINE ---
        binding.btnLogout.setOnClickListener {
            handleLogout()
        }

        return view
    }


    private fun displayUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvUserEmail.text = user.email ?: "No email associated"
        } else {
            // This case should ideally not happen if the user is on this screen
            binding.tvUserEmail.text = "User not logged in"
        }
    }

    private fun handleChangePassword() {
        val user = auth.currentUser
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // --- Validations ---
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Please fill in both password fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Update Password in Firebase ---
        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("ProfileFragment", "User password updated.")
                Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                // Clear the fields after success
                binding.etNewPassword.text?.clear()
                binding.etConfirmPassword.text?.clear()
            } else {
                Log.e("ProfileFragment", "Password update failed", task.exception)
                // Firebase may require the user to have recently logged in.
                // This is a security measure.
                Toast.makeText(context, "Failed to update password. Please try logging out and in again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleLogout() {
        auth.signOut()
        // Navigate back to the Login activity
        val intent = Intent(activity, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}


