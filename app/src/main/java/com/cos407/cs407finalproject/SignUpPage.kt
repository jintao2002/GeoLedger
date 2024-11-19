package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpPage : AppCompatActivity() {

    // Lazy initialization of UserDao
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    // Firebase Firestore instance
    private val firebaseDb by lazy { FirebaseFirestore.getInstance() }

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
            val intent = Intent(this, SignInPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.etLastName).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            // Validate fields
            if (isValidEmail(email) && isValidPassword(password) && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                // Register user in the database and Firebase
                registerUser(firstName, lastName, email, password)
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
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(this, "First and Last Name are required", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Function to register user by saving details in Room database and Firebase
    private fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Create a new User instance with the provided details
            val newUser =
                User(firstName = firstName, lastName = lastName, email = email, password = password)

            // Insert the user into the local database
            userDao.insertUser(newUser)

            // Save the user to Firebase Firestore
            firebaseDb.collection("users").add(newUser).addOnSuccessListener {
                // Log success or notify UI
                Log.d("SignUpPage", "User saved to Firebase successfully")
            }.addOnFailureListener { e ->
                // Log failure or handle error
                Log.e("SignUpPage", "Error saving user to Firebase", e)
            }

            // Show success message and navigate to sign-in page
            runOnUiThread {
                Toast.makeText(this@SignUpPage, "Registration successful!", Toast.LENGTH_SHORT)
                    .show()

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@SignUpPage, SignInPage::class.java)
                    startActivity(intent)
                    finish() // Close the Sign Up page
                }, 1000)
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
