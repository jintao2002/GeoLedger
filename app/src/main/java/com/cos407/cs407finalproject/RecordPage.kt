package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class RecordPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Record button: stays on the current page
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            // Do nothing, already on Record page
        }

//        // Summary button: Go to the Summary page.
//        findViewById<Button>(R.id.btnSummary).setOnClickListener {
//            val intent = Intent(this, SummaryPage::class.java)
//            startActivity(intent)
//        }

        // Me button: Go to the Me page
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            val intent = Intent(this, MePage::class.java)
            startActivity(intent)
        }
    }
}
