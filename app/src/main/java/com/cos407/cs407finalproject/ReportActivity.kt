package com.cos407.cs407finalproject

import android.os.Bundle
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

        // Create FAQ content programmatically
        val container = findViewById<LinearLayout>(R.id.faqContainer)

        // Add FAQ items
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
}