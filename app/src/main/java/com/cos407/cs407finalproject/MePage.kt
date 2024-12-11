package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cos407.cs407finalproject.database.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MePage : AppCompatActivity() {

    // Lazy initialization of UserDao
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    // Firebase Firestore instance
    private val firebaseDb by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_page)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Retrieve userId from SharedPreferences
        val userId = getCurrentUserId()

        if (userId != -1) {
            // Load user data using userId
            CoroutineScope(Dispatchers.IO).launch {
                fetchUserData(userId) { userName ->
                    runOnUiThread {
                        tvUserName.text = userName ?: "Username"
                    }
                }
            }
        } else {
            tvUserName.text = "Username"
        }

        // Bottom Navigation
        setupBottomNavigation()

        // Logout button: clears the stored userId and returns to MainActivity
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            clearCurrentUserId()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup Settings button
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsPage::class.java)
            startActivity(intent)
        }

        // Setup Add Budget button
        findViewById<Button>(R.id.btnAddBudget).setOnClickListener {
            val intent = Intent(this, AddBudget::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnMapView).setOnClickListener {
            startActivity(Intent(this, MapViewActivity::class.java))
        }

        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            startActivity(Intent(this, RecordPage::class.java))
        }

    }

    // Fetch user data from Room and Firebase
    private fun fetchUserData(userId: Int, onResult: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserById(userId)

            if (user != null) {
                onResult("${user.firstName} ${user.lastName}")
            } else {
                firebaseDb.collection("users").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { documents ->
                        val firebaseUser = documents.firstOrNull()
                        val userName = firebaseUser?.let {
                            "${it.getString("firstName")} ${it.getString("lastName")}"
                        }
                        onResult(userName)
                    }.addOnFailureListener {
                        onResult(null)
                    }
            }
        }
    }

    // Bottom Navigation Setup
    private fun setupBottomNavigation() {
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSummary).setOnClickListener {
            val intent = Intent(this, SummaryPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnMe).setOnClickListener {
            // Do nothing as it’s the current page
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