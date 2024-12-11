package com.cos407.cs407finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        // Set up action bar with back button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Terms of Use"
        }
    }

    // Handle back button in action bar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}