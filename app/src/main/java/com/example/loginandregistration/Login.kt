package com.example.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.loginandregistration.admin.AdminDashboardActivity
import com.example.loginandregistration.security.ui.SecurityMainActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val oneTapLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Log.w(TAG, "Google sign in failed: ID token is null")
                    Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Google sign in cancelled or failed: resultCode=${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")

                        // --- FIX: Special check for the security email ---
                        if (email == "security@gmail.com") {
                            Toast.makeText(this, "Security Login Successful", Toast.LENGTH_SHORT).show()
                            navigateTo(SecurityMainActivity::class.java)
                        } else {
                            // Use the existing role check for all other users
                            checkUserRoleAndRedirect()
                        }
                        // --- END FIX ---

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, "beginSignIn failed: ${e.localizedMessage}")
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        Log.d(TAG, "New user detected, creating Firestore document.")
                        auth.currentUser?.let { createNewUserDocument(it) }
                    }
                    checkUserRoleAndRedirect()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, getString(R.string.google_login_failed, task.exception?.message ?: getString(R.string.unknown_error)), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserRoleAndRedirect() {
        val user = auth.currentUser ?: return
        val userEmail = user.email

        // --- FIX: Added special check for security email in the central function ---
        if (userEmail == "security@gmail.com") {
            navigateTo(SecurityMainActivity::class.java)
            return
        }
        // --- END FIX ---

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    when (document.getString("role")) { // Assuming the field is 'role' based on your provided structure
                        "Admin" -> navigateTo(AdminDashboardActivity::class.java)
                        "Security" -> navigateTo(SecurityMainActivity::class.java)
                        "Student" -> navigateTo(MainActivity::class.java) // Changed from "User" to "Student"
                        else -> {
                            Log.w(TAG, "Role not recognized. Defaulting to MainActivity.")
                            navigateTo(MainActivity::class.java)
                        }
                    }
                } else {
                    Log.w(TAG, "User document does not exist for UID: ${user.uid}. Creating and redirecting.")
                    createNewUserDocument(user)
                    navigateTo(MainActivity::class.java)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user role", exception)
                navigateTo(MainActivity::class.java)
            }
    }

    private fun createNewUserDocument(user: FirebaseUser) {
        val userMap = hashMapOf(
            "email" to user.email,
            "role" to "Student", // Default role changed to 'Student'
            "displayName" to user.displayName,
            "uid" to user.uid
        )

        db.collection("users").document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                Log.d(TAG, "New user document created in Firestore for UID: ${user.uid}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user document in Firestore", e)
            }
    }

    private fun <T : Activity> navigateTo(activityClass: Class<out T>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already logged in. Checking role...")
            checkUserRoleAndRedirect()
        }
    }
}
