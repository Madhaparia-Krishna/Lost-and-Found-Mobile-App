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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // --- CHANGE START: Use the new One Tap client ---
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    // --- CHANGE END ---
    
    private var keepSplashScreen = true

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
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Set theme back to regular theme (for Android 11 and below)
        setTheme(R.style.Theme_LoginAndRegistration)
        
        // Set condition to keep splash screen visible
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        // Launch coroutine to dismiss splash screen after shorter delay
        lifecycleScope.launch {
            kotlinx.coroutines.delay(800) // Reduced from 1500ms to 800ms
            keepSplashScreen = false
        }
        
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
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

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

        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val etResetEmail = dialogView.findViewById<EditText>(R.id.etResetEmail)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.send_reset_email) { _, _ ->
                val email = etResetEmail.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, R.string.error_empty_email, Toast.LENGTH_SHORT).show()
                } else {
                    sendPasswordResetEmail(email)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent to: $email")
                    Toast.makeText(
                        this,
                        R.string.password_reset_email_sent,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.w(TAG, "Failed to send password reset email", task.exception)
                    Toast.makeText(
                        this,
                        getString(R.string.password_reset_failed, task.exception?.message ?: getString(R.string.unknown_error)),
                        Toast.LENGTH_LONG
                    ).show()
                }
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
        val currentUser = auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        // Use lifecycleScope to launch coroutine on IO dispatcher
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val document = db.collection("users").document(currentUser.uid)
                    .get()
                    .await()
                
                withContext(Dispatchers.Main) {
                    if (document.exists()) {
                        // Check if user is blocked
                        val isBlocked = document.getBoolean("isBlocked") ?: false
                        if (isBlocked) {
                            // User is blocked, sign them out and show error message
                            auth.signOut()
                            Toast.makeText(
                                this@Login,
                                "Your account has been blocked. Please contact support.",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.w(TAG, "Blocked user attempted to login: ${currentUser.uid}")
                            return@withContext
                        }
                        
                        // User is not blocked, proceed with role-based navigation
                        val role = document.getString("role") ?: "USER"
                        when (role.uppercase()) {
                            "ADMIN" -> {
                                val intent = Intent(this@Login, com.example.loginandregistration.admin.AdminDashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            "SECURITY" -> {
                                val intent = Intent(this@Login, SecurityMainActivity::class.java)
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error checking user role", e)
                    Toast.makeText(this@Login, "Error checking user role: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createNewUserDocument(userId: String, email: String?) {
        val db = FirebaseFirestore.getInstance()
        
        // Create User object with available data
        val user = User(
            uid = userId,
            displayName = "", // Empty for existing users, they can update in profile
            email = email ?: "",
            phone = "", // Empty for existing users, they can update in profile
            gender = "",
            fcmToken = "",
            createdAt = com.google.firebase.Timestamp.now(),
            updatedAt = com.google.firebase.Timestamp.now()
        )
        
        // Use lifecycleScope to launch coroutine on IO dispatcher
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("users").document(userId)
                    .set(user)
                    .await()
                Log.d(TAG, "User document created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating user document", e)
            }
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

