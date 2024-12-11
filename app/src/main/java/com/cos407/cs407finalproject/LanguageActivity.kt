package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

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
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}