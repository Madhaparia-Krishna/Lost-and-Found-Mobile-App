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
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // --- CHANGE START: Use the new One Tap client ---
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    // --- CHANGE END ---

    companion object {
        private const val TAG = "LoginActivity"
    }

    // --- CHANGE START: Update the activity result launcher ---
    private val oneTapLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                // Get the credential from the result
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    // Pass the ID token to Firebase
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
    // --- CHANGE END ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        // --- CHANGE START: Configure the new One Tap Sign-In ---
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Show all accounts on the device
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        // --- CHANGE END ---


        // Find Views
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        // Set Click Listeners
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in with Email and Password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        checkUserRoleAndRedirect()
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
        // --- CHANGE START: Use the new One Tap client to begin sign-in ---
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
                // No Google Accounts found. Fallback to regular sign-in?
                Log.d(TAG, "beginSignIn failed: ${e.localizedMessage}")
            }
        // --- CHANGE END ---
    }


    // --- CHANGE START: Update this function to accept the ID token string ---
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        // --- CHANGE END ---
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    // Check if the user is new to create their document
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        Log.d(TAG, "New user detected, creating Firestore document.")
                        createNewUserDocument(auth.currentUser!!.uid, auth.currentUser?.email)
                    }
                    checkUserRoleAndRedirect()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, getString(R.string.google_login_failed, task.exception?.message ?: getString(R.string.unknown_error)), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun checkUserRoleAndRedirect() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No user logged in")
            return
        }
        
        // Check if user is admin
        if (currentUser.email == "admin@gmail.com") {
            Log.d(TAG, "Admin user detected, navigating to admin dashboard")
            navigateToAdminDashboard()
        } else {
            Log.d(TAG, "Regular user detected, navigating to main dashboard")
            navigateToDashboard()
        }
    }
    
    private fun navigateToAdminDashboard() {
        val intent = Intent(this, com.example.loginandregistration.admin.AdminDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun createNewUserDocument(userId: String, email: String?) {
        val db = FirebaseFirestore.getInstance()
        val userDoc = hashMapOf(
            "email" to (email ?: ""),
            "role" to "user",
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("users").document(userId)
            .set(userDoc)
            .addOnSuccessListener {
                Log.d(TAG, "User document created successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error creating user document", e)
            }
    }

    private fun checkUserRoleAndRedirect() {
        val currentUser = auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "user"
                    when (role) {
                        "admin" -> {
                            val intent = Intent(this, com.example.loginandregistration.admin.AdminDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        "security" -> {
                            val intent = Intent(this, SecurityMainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            navigateToDashboard()
                        }
                    }
                } else {
                    // Document doesn't exist, create it
                    createNewUserDocument(currentUser.uid, currentUser.email)
                    navigateToDashboard()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking user role", e)
                Toast.makeText(this, "Error checking user role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNewUserDocument(userId: String, email: String?) {
        val db = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "email" to (email ?: ""),
            "role" to "user",
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User document created successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user document", e)
            }
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

