package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Get references to the EditText fields
        val nameEditText = findViewById<EditText>(R.id.editTextName)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)

        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)

        val addressEditText = findViewById<EditText>(R.id.editTextAddress)

        // Handle save button click
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = nameEditText.text.toString()

            val email = emailEditText.text.toString()

            val phone = phoneEditText.text.toString()

            val address = addressEditText.text.toString()

            // TODO: Implement save functionality (e.g., save to database or shared preferences)

            Toast.makeText(
                this,
                "Profile saved: $name, $email, $phone, $address",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}