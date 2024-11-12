package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cos407.cs407finalproject.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MePage : AppCompatActivity() {

    // Lazy initialization of UserDao
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_page)

        // Get the username and set it
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Retrieve email passed from SignInPage
        val email = intent.getStringExtra("USER_EMAIL")

        email?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserByEmail(it) // Retrieve user by email
                runOnUiThread {
                    // Display the user's full name if available, otherwise use a placeholder
                    tvUserName.text = user?.let { "${it.firstName} ${it.lastName}" } ?: "Username"
                }
            }
        }

        // Record button: jumps to the Record page
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        // Me button: stays on the current page
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            // Already on Me page, no action required
        }

        // Logout button: performs a logout operation.
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
