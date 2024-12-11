package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class AppearanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)

        val slideMenu = findViewById<SlideMenu>(R.id.slideMenu)
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Toggle side menu on profile button click
        profileButton.setOnClickListener {
            slideMenu.switchMenu()
        }

        // Set up side menu navigation buttons
        setupMenuButtons()

        // Initialize theme switch based on current theme
        val isDarkTheme = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        findViewById<Switch>(R.id.switchTheme).isChecked = isDarkTheme

        // Handle theme changes
        findViewById<Switch>(R.id.switchTheme).setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
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
            // Do nothing
        }

        findViewById<LinearLayout>(R.id.menuReport)?.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuTerms)?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }
}
