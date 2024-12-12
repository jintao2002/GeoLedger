package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

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

        val themeSwitch = findViewById<SwitchCompat>(R.id.switchTheme)
        
        // check current theme
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        Log.d("AppearanceActivity", "Current theme mode: $currentMode")
        
        themeSwitch.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            try {
                val newMode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                Log.d("AppearanceActivity", "Switching to mode: $newMode")
                
                // apply theme
                AppCompatDelegate.setDefaultNightMode(newMode)
                
                // save settings
                getSharedPreferences("Settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("DarkTheme", isChecked)
                    .apply()
                
                Log.d("AppearanceActivity", "Theme switch successful")
                

                Handler(Looper.getMainLooper()).postDelayed({
                    recreate()
                }, 100)
            } catch (e: Exception) {
                Log.e("AppearanceActivity", "Theme switch failed", e)
                e.printStackTrace()
            }
        }

        // Set up side menu navigation buttons
        setupMenuButtons()
    }

    private fun setupMenuButtons() {
        findViewById<LinearLayout>(R.id.menuRecord)?.setOnClickListener {
            startActivity(Intent(this, RecordPage::class.java))
        }

        findViewById<LinearLayout>(R.id.menuLanguage)?.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuAppearance)?.setOnClickListener {
            // Do nothing as we're already in Appearance
        }

        findViewById<LinearLayout>(R.id.menuReport)?.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.menuTerms)?.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }
}
