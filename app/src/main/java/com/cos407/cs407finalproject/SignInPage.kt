package com.cos407.cs407finalproject

import android.content.Context
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
import com.cos407.cs407finalproject.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInPage : AppCompatActivity() {

    // Lazy initialization of UserDao for database access
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

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
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            val enteredEmail = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val enteredPassword = findViewById<EditText>(R.id.etPassword).text.toString()

            // Validate login by checking database for matching credentials
            validateUserCredentials(enteredEmail, enteredPassword)
        }
    }

    // Function to validate user credentials using Room database
    private fun validateUserCredentials(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserByEmail(email) // Query the database for the user
            runOnUiThread {
                if (user != null && user.password == password) {
                    // Save userId to SharedPreferences
                    saveUserIdToPreferences(user.userId)

                    // Show success message and navigate to MePage
                    Toast.makeText(this@SignInPage, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignInPage, MePage::class.java).apply {
                        putExtra("USER_ID", user.userId) // Pass the user's ID to MePage
                    }
                    startActivity(intent)
                    finish() // Close the Sign In page
                } else {
                    // Show error message if login fails
                    Toast.makeText(
                        this@SignInPage, "Incorrect email or password", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Save userId to SharedPreferences for global access
    private fun saveUserIdToPreferences(userId: Int) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("CURRENT_USER_ID", userId).apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.page_menu, menu)
        return true
    }
}
