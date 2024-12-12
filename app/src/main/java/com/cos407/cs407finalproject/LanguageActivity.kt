package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val slideMenu = findViewById<SlideMenu>(R.id.slideMenu)
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Toggle side menu on profile button click
        profileButton.setOnClickListener {
            slideMenu.switchMenu()
        }

        // Set up side menu navigation buttons
        setupMenuButtons()

        // Initialize language selection based on current locale
        val currentLanguage = Locale.getDefault().language
        findViewById<RadioGroup>(R.id.languageGroup).check(
            if (currentLanguage == "zh") R.id.rbChinese else R.id.rbEnglish
        )

        // Handle language changes
        findViewById<RadioGroup>(R.id.languageGroup).setOnCheckedChangeListener { _, checkedId ->
            val locale = when (checkedId) {
                R.id.rbChinese -> Locale("zh")
                else -> Locale("en")
            }
            updateLocale(locale)
        }
    }

    private fun updateLocale(locale: Locale) {
        // Save language setting
        val editor = getSharedPreferences("Settings", MODE_PRIVATE).edit()
        editor.putString("Language", locale.language)
        editor.apply()

        // Update configuration
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate current activity instead of going back to MainActivity
        recreate()
    }

    /**
     * Set up side menu buttons for navigation
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
            // Do nothing
        }

        findViewById<LinearLayout>(R.id.menuAppearance)?.setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuReport)?.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuTerms)?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }
}
