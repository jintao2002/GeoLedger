package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Set up action bar with back button
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Help & Support"
        }

        val slideMenu = findViewById<SlideMenu>(R.id.slideMenu)
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Toggle side menu on profile button click
        profileButton.setOnClickListener {
            slideMenu.switchMenu()
        }

        // Set up side menu buttons
        setupMenuButtons()

        // Create FAQ content programmatically
        val container = findViewById<LinearLayout>(R.id.faqContainer)
        addFaqItem(container, getString(R.string.faq_1_title), getString(R.string.faq_1_solution))
        addFaqItem(container, getString(R.string.faq_2_title), getString(R.string.faq_2_solution))
        addFaqItem(container, getString(R.string.faq_3_title), getString(R.string.faq_3_solution))
    }

    private fun addFaqItem(container: LinearLayout, title: String, solution: String) {
        val itemView = layoutInflater.inflate(R.layout.item_faq, container, false)
        itemView.findViewById<TextView>(R.id.tvFaqTitle).text = title
        itemView.findViewById<TextView>(R.id.tvFaqSolution).text = solution
        container.addView(itemView)
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
            // do nothing
        }

        findViewById<LinearLayout>(R.id.menuTerms)?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }
}
