package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.User
import com.cos407.cs407finalproject.database.UserDao
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        userDao = AppDatabase.getDatabase(this).userDao()

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val addressEditText = findViewById<EditText>(R.id.editTextAddress)


        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val address = addressEditText.text.toString()


            val user = User(
                firstName = name,
                lastName = "",
                email = email,
                password = ""
            )


            saveUser(user)

            Toast.makeText(this, "Profile saved: $name, $email, $phone, $address", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveUser(user: User) {
        lifecycleScope.launch {
            userDao.insertUser(user)
        }
    }
}