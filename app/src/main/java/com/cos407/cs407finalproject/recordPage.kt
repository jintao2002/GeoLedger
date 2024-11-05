package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class recordPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Find the Logout button and set up a click listener
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Create an Intent to navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the recordPage
        }

        // Find the button that leads to slide.
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Set up the intent
        profileButton.setOnClickListener {
            val intent = Intent(this, slideActivity::class.java)
            startActivity(intent)
        }
    }
}