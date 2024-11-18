package com.cos407.cs407finalproject

import android.content.Context
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

        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Retrieve userId from SharedPreferences
        val userId = getCurrentUserId()

        if (userId != -1) {
            // Load user data using userId
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUserById(userId) // Assume a new DAO method getUserById
                runOnUiThread {
                    tvUserName.text = user?.let { "${it.firstName} ${it.lastName}" } ?: "Username"
                }
            }
        } else {
            tvUserName.text = "Username"
        }

        // Navigate to Record page
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        // Logout button: clears the stored userId and returns to MainActivity
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            clearCurrentUserId()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Retrieve current userId from SharedPreferences
    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1) // -1 means no user logged in
    }

    // Clear current userId from SharedPreferences
    private fun clearCurrentUserId() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("CURRENT_USER_ID").apply()
    }
}
