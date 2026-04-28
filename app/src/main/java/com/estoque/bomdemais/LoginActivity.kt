package com.estoque.bomdemais

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signupName: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, redirect to MainActivity
            redirectToMainActivity()
            return // Skip the rest of the onCreate method
        }

        setContentView(R.layout.activity_login)

        // Initialize UI components
        signupName = findViewById(R.id.signup_name)
        signupPassword = findViewById(R.id.signup_password)
        signupButton = findViewById(R.id.signup_button)

        // Set onClickListener for the login button
        signupButton.setOnClickListener {
            loginUser()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Optional: Close the current activity
    }

    private fun loginUser() {
        val email = signupName.text.toString().trim()
        val password = signupPassword.text.toString().trim()

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

    // Sign in with Firebase Authentication
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Login success
                Toast.makeText(this, "Logado com sucesso!", Toast.LENGTH_SHORT).show()
                // Redirect to MainActivity
                redirectToMainActivity()
            } else {
                // If sign in fails, display a message to the user
                Toast.makeText(this, "Senha ou nome de usuário invalidos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}