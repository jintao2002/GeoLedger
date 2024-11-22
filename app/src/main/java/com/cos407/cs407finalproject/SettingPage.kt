package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_page)

        // Handle back button click
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Add click listeners for other settings buttons
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            // TODO: Implement edit profile functionality
        }

        findViewById<Button>(R.id.btnNotifications).setOnClickListener {
            // TODO: Implement notifications settings
        }

        findViewById<Button>(R.id.btnCurrency).setOnClickListener {
            // TODO: Implement currency settings
        }
    }
}