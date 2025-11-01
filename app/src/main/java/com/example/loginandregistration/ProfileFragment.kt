package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.loginandregistration.databinding.FragmentProfileBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var scrollView: android.widget.ScrollView
    
    private var currentUser: User? = null
    private val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        // Initialize loading views
        progressBar = view.findViewById(R.id.progress_bar)
        scrollView = view.findViewById(R.id.scroll_view)

        // Set up gender dropdown
        setupGenderDropdown()

        // Load user profile data
        loadUserProfile()

        // Set up button click listeners
        binding.btnSaveProfile.setOnClickListener {
            handleSaveProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        binding.btnLogout.setOnClickListener {
            handleLogout()
        }

        return view
    }

    private fun setupGenderDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.actvGender.setAdapter(adapter)
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                hideLoading()
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    populateUserData(currentUser)
                } else {
                    // User document doesn't exist, create one with basic info
                    createUserDocument(user.uid, user.email ?: "")
                }
            }
            .addOnFailureListener { e ->
                hideLoading()
                Log.e("ProfileFragment", "Error loading user profile", e)
                Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                // Still populate email from auth
                binding.etEmail.setText(user.email ?: "")
            }
    }

    private fun createUserDocument(userId: String, email: String) {
        val newUser = User(
            userId = userId,
            email = email,
            name = "",
            phone = "",
            gender = "",
            fcmToken = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        db.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                currentUser = newUser
                populateUserData(newUser)
                Log.d("ProfileFragment", "User document created")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error creating user document", e)
                // Still populate email
                binding.etEmail.setText(email)
            }
    }

    private fun populateUserData(user: User?) {
        if (user != null) {
            binding.etName.setText(user.name)
            binding.etEmail.setText(user.email)
            binding.etPhone.setText(user.phone)
            
            // Set gender if it exists
            if (user.gender.isNotEmpty() && genderOptions.contains(user.gender)) {
                binding.actvGender.setText(user.gender, false)
            }
        }
    }
    private fun handleSaveProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val gender = binding.actvGender.text.toString().trim()

        // Validate required fields
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        } else {
            binding.tilName.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            return
        } else {
            binding.tilPhone.error = null
        }

        // Validate phone number format (basic validation)
        if (phone.length < 10) {
            binding.tilPhone.error = "Please enter a valid phone number"
            return
        } else {
            binding.tilPhone.error = null
        }

        showLoading()

        // Update user document in Firestore
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "gender" to gender,
            "updatedAt" to Timestamp.now()
        )

        db.collection("users").document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                hideLoading()
                // Update current user object
                currentUser = currentUser?.copy(
                    name = name,
                    phone = phone,
                    gender = gender,
                    updatedAt = Timestamp.now()
                )
                Log.d("ProfileFragment", "Profile updated successfully")
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                hideLoading()
                Log.e("ProfileFragment", "Error updating profile", e)
                Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
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

        // Show loading indicator
        showLoading()

        // --- Update Password in Firebase ---
        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            hideLoading()
            
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

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        binding.btnSaveProfile.isEnabled = false
        binding.btnChangePassword.isEnabled = false
        binding.btnLogout.isEnabled = false
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = true
        binding.btnChangePassword.isEnabled = true
        binding.btnLogout.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}


