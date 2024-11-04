package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class signInPage : AppCompatActivity() {

    // Sample username and password for validation
    // Place Holder to be replaced
    private val validUsername = "uwm@wisc.edu"
    private val validPassword = "123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Navigate to Sign Up page when 'Need an account?' is clicked
        findViewById<Button>(R.id.btnNeedAccount).setOnClickListener {
            val intent = Intent(this, signUpPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            val enteredUsername = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val enteredPassword = findViewById<EditText>(R.id.etPassword).text.toString()

            // Check if username and password are correct
            if (enteredUsername == validUsername && enteredPassword == validPassword) {
                // Show success message and navigate to recordPage
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, recordPage::class.java)
                startActivity(intent)
                finish() // Close the Sign In page
            } else {
                // Show error message if login fails
                Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.page_menu, menu)
        return true
    }
}