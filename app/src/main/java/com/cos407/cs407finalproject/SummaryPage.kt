package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SummaryPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_page) // Ensure this layout file matches your XML file name

        // Find the buttons
        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val btnSummary = findViewById<Button>(R.id.btnSummary)
        val btnMe = findViewById<Button>(R.id.btnMe)

        // Set up click listeners
        btnRecord.setOnClickListener {
            // Navigate to RecordPage
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        btnSummary.setOnClickListener {
            // Navigate to SummaryPage (current page)
            // Do nothing or refresh
        }

        btnMe.setOnClickListener {
            // Navigate to MePage
            val intent = Intent(this, MePage::class.java)
            startActivity(intent)
        }
    }
}
