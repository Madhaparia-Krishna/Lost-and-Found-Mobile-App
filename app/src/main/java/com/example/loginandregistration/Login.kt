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
                    Toast.makeText(this, "Google login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Checks the logged-in user's role in Firestore and redirects them to the appropriate activity.
     */
    private fun checkUserRoleAndRedirect() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not found! Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Document exists, get the role
                val userRole = document.getString("role")
                Log.d(TAG, "User role found: $userRole")

                when (userRole) {
                    "Admin", "Security" -> {
                        // For Admin and Security, go to the security dashboard
                        val intent = Intent(this, SecurityMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    "Student" -> {
                        // For Student, go to the main dashboard
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    else -> {
                        // Fallback for any other role or if the role is null
                        Toast.makeText(this, "Role not recognized, using default dashboard.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
                finish() // Close the login activity after redirection
            } else {
                // This case handles users who signed up but for some reason don't have a doc.
                // The new user case is now handled right after successful Google login.
                Log.w(TAG, "User document does not exist for UID: $userId. Creating default 'Student' user.")
                createNewUserDocument(userId, auth.currentUser?.email)

                // Redirect them to the main student activity.
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting user data from Firestore.", exception)
            Toast.makeText(this, "Failed to get user data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates a new user document in Firestore, defaulting the role to "Student".
     * This is useful for users signing in for the first time.
     */
    private fun createNewUserDocument(userId: String, email: String?) {
        val firestore = FirebaseFirestore.getInstance()
        val newUser = hashMapOf(
            "uid" to userId,
            "email" to (email ?: ""),
            "role" to "Student", // Default role for any new user
            "displayName" to (auth.currentUser?.displayName ?: ""),
            "photoUrl" to (auth.currentUser?.photoUrl?.toString() ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully created new user document for UID: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to create new user document.", e)
            }
    }

    /**
     * Checks if a user is already signed in when the activity starts.
     */
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already logged in. Checking role...")
            checkUserRoleAndRedirect()
        }
    }
}

