package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            showNotificationSettingsDialog()
        }

        findViewById<Button>(R.id.btnCurrency).setOnClickListener {
            // TODO: Implement currency settings
        }
    }

    private fun showNotificationSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Notification Settings")
        builder.setMessage("Do you want to enable notifications?")

        builder.setPositiveButton("Enable") { dialog, _ ->
            // Logic to enable notifications
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Disable") { dialog, _ ->
            // Logic to disable notifications
            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}