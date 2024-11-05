package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class recordPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Find the Logout button and set up a click listener
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Create an Intent to navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the recordPage
        }

        // 找到触发侧滑的按钮或图片
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // 设置点击事件
        profileButton.setOnClickListener {
            val intent = Intent(this, slideActivity::class.java)
            startActivity(intent)
        }
    }
}