package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class signUpPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack2).setOnClickListener {
            finish()
        }

        // Navigate to Sign In page when 'Already have an account?' is clicked
        findViewById<Button>(R.id.btnAlreadyHaveAccount).setOnClickListener {
            val intent = Intent(this, signInPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            // Validate email and password
            if (isValidEmail(email) && isValidPassword(password)) {
                // Show success message
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                // Delay for 1 second and then navigate to the Sign In page
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, signInPage::class.java)
                    startActivity(intent)
                    finish() // Close the Sign Up page
                }, 1000) // 1000 milliseconds = 1 second
            } else {
                // Show error messages if validation fails
                if (!isValidEmail(email)) {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
                if (!isValidPassword(password)) {
                    Toast.makeText(
                        this, "Password must be at least 6 characters", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Function to check if the email format is valid
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to check if the password length is at least 6 characters
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}