package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatDelegate
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.repository.RecordRepository
import com.cos407.cs407finalproject.viewmodel.RecordViewModel
import com.cos407.cs407finalproject.viewmodel.RecordViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    private val recordViewModel: RecordViewModel by viewModels {
        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        RecordViewModelFactory(repository) // <-- Use repository here
    }

     override fun attachBaseContext(newBase: Context) {
        // Load saved language preference before creating activity
        val preferences = newBase.getSharedPreferences("Settings", MODE_PRIVATE)
        val language = preferences.getString("Language", "en") ?: "en"
        
        // Create configuration with saved language
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        
        super.attachBaseContext(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // saved theme setting
        val isDarkTheme = getSharedPreferences("Settings", MODE_PRIVATE)
            .getBoolean("DarkTheme", false)
        
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        setContentView(R.layout.activity_main)

        // Handle edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigate to the Sign Up page
        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
        }

        // Navigate to the Sign In page
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            val intent = Intent(this, SignInPage::class.java)
            startActivity(intent)
        }

        //Click the "Get Started" to navigate to the Sign Up page
        findViewById<Button>(R.id.signUpButton).setOnClickListener {
            val intent = Intent(this, SignUpPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            val intent = Intent(this, SignInPage::class.java)
            startActivity(intent)
        }
    }

    // Set up the menu bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.page_menu, menu)
        return true
    }

    // Handle menu bar item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item1 -> {
                Toast.makeText(this, "Item 1 selected", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.item2 -> {
                Toast.makeText(this, "Item 2 selected", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.item3 -> {
                Toast.makeText(this, "Item 3 selected", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
