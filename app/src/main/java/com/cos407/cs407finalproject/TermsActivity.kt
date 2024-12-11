package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
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

        val slideMenu = findViewById<SlideMenu>(R.id.slideMenu)
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Toggle side menu on profile button click
        profileButton.setOnClickListener {
            slideMenu.switchMenu()
        }

        // Set up side menu buttons
        setupMenuButtons()
    }

    // Handle back button in action bar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Set up side menu buttons for navigation.
     * Record -> RecordPage
     * Language -> LanguageActivity
     * Appearance -> AppearanceActivity
     * Report -> ReportActivity
     * Terms -> TermsActivity
     */
    private fun setupMenuButtons() {
        findViewById<LinearLayout>(R.id.menuRecord)?.setOnClickListener {
            startActivity(Intent(this, RecordPage::class.java))
        }

        findViewById<LinearLayout>(R.id.menuLanguage)?.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuAppearance)?.setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuReport)?.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuTerms)?.setOnClickListener {
            // Do nothing
        }
    }
}
