package hk.hku.cs.swifttrip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var signInButton: MaterialButton
    private lateinit var createAccountButton: MaterialButton

    private var creatingAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        // ✅ Set system bar white with dark icons
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_light)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        auth = FirebaseAuth.getInstance()

        // 🔹 Configure Google Sign-In
        val clientId = getString(R.string.default_web_client_id)
        if (clientId.isBlank()) {
            Toast.makeText(this, "Missing Google Client ID!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 🔹 If already logged in, go directly to Main
        auth.currentUser?.let {
            goToMainActivity()
            return
        }

        // ---------- UI SETUP ----------
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordInputLayout)
        signInButton = findViewById(R.id.emailSignInButton)
        createAccountButton = findViewById(R.id.createAccountButton)

        // Google button
        val googleButton = findViewById<SignInButton>(R.id.googleSignInButton)
        googleButton.setOnClickListener { signInWithGoogle() }

        // Email/Password Sign In
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (creatingAccount) {
                val confirm = confirmPasswordEditText.text.toString().trim()
                if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password != confirm) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                createAccount(email, password)
            } else {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                signInWithEmail(email, password)
            }
        }

        // Toggle between Sign In / Create Account
        createAccountButton.setOnClickListener {
            creatingAccount = !creatingAccount
            if (creatingAccount) {
                confirmPasswordLayout.visibility = View.VISIBLE
                signInButton.text = getString(R.string.btn_create_account)
                createAccountButton.text = getString(R.string.btn_back_to_login)
            } else {
                confirmPasswordLayout.visibility = View.GONE
                signInButton.text = getString(R.string.btn_sign_in)
                createAccountButton.text = getString(R.string.btn_create_account)
            }
        }
    }

    // 🔹 Google Sign-In Flow
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.w("SignInActivity", "Google sign in failed", e)
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 🔹 Email/Password Auth
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Invalid credentials or account not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // 🔹 Navigate to Main Activity
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
