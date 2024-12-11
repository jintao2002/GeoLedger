package com.cos407.cs407finalproject

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddBudget : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)


        db = FirebaseFirestore.getInstance()

        // Setup category spinner
        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        ArrayAdapter.createFromResource(
            this,
            R.array.budget_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Handle save button click
        findViewById<Button>(R.id.btnSaveBudget).setOnClickListener {
            val budgetName = findViewById<EditText>(R.id.etBudgetName).text.toString()
            val budgetAmount = findViewById<EditText>(R.id.etBudgetAmount).text.toString()
            val category = spinner.selectedItem.toString()

            if (budgetName.isBlank() || budgetAmount.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val budget = hashMapOf(
                "name" to budgetName,
                "amount" to budgetAmount,
                "category" to category
            )

            db.collection("budgets")
                .add(budget)
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle back button click
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}