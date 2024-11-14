package com.cos407.cs407finalproject

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.Record
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar

class RecordPage : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private val REQUEST_CODE_PLACE_PICKER = 1000
    private var selectedLocation: String = "Location not selected" // Store selected location
    private var selectedDate: String = "Date not selected" // Store selected date
    private var currentDialogView: View? = null // Reference to the current dialog view
    private var alertDialog: AlertDialog? = null
    private lateinit var database: AppDatabase // Database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Initialize tableLayout after setting content view
        tableLayout = findViewById(R.id.tableLayout)

        // Initialize the Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBL3lLEc6S0OTaBiwe6j2vVMfDcP7qmd8w")
            Log.d("PlacesAPI", "Google Places API initialized")
        }

        val placesClient = Places.createClient(this)

        // Initialize Room database
        database = AppDatabase.getDatabase(this)

        // Load saved records from the database on startup
        loadRecords()

        // FloatingActionButton to open the add record dialog
        findViewById<FloatingActionButton>(R.id.fabAddRecord).setOnClickListener {
            showAddRecordDialog()
        }

        // Record button: opens input dialog for new record
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            showAddRecordDialog()
        }

        // Uncomment if needed: Summary button goes to the Summary page
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

    private fun showAddRecordDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)
        currentDialogView = dialogView  // Store reference to the dialog view

        val etItem = dialogView.findViewById<EditText>(R.id.etItem)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvLocation = dialogView.findViewById<TextView>(R.id.tvLocation)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        // Set initial text for location and date fields
        tvLocation.text = selectedLocation
        tvDate.text = selectedDate

        // Set location button to open place picker
        dialogView.findViewById<Button>(R.id.btnSelectLocation).setOnClickListener {
            openPlacePicker()
        }

        // Set date button to open date picker
        dialogView.findViewById<Button>(R.id.btnSelectDate).setOnClickListener {
            openDatePicker(tvDate)
        }

        // Set up custom Add button
        dialogView.findViewById<Button>(R.id.btnAddRecord).setOnClickListener {
            val item = etItem.text.toString()
            val amount = etAmount.text.toString()
            val record = Record(
                amount = amount,
                item = item,
                location = selectedLocation,
                date = selectedDate
            )
            saveRecord(record) // Save the record to the database
            addRecord(record) // Add record to the table layout
            currentDialogView = null  // Clear dialog reference after adding record
            alertDialog?.dismiss() // Close dialog
        }

        // Set up custom Cancel button
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            currentDialogView = null  // Clear dialog reference if canceled
            alertDialog?.dismiss() // Close dialog
        }

        // Show the dialog without the default buttons
        alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Record")
            .setView(dialogView)
            .create()
        alertDialog?.show()
    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("US")  // Optionally restrict to a specific country
            .build(this)
        startActivityForResult(intent, REQUEST_CODE_PLACE_PICKER)
    }


    // Handle the result from the place picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PLACE_PICKER && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            selectedLocation = place.name ?: "Unknown location"

            // Update the selected location in the dialog if it's open
            currentDialogView?.findViewById<TextView>(R.id.tvLocation)?.text = selectedLocation
        }
    }

    // Function to open date picker dialog
    private fun openDatePicker(tvDate: TextView) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = "$year-${month + 1}-$dayOfMonth"
                tvDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Function to add a new record to the table
    fun addRecord(record: Record) {
        val tableRow = TableRow(this)

        // Create and configure the item TextView
        val itemTextView = TextView(this)
        itemTextView.text = record.item
        itemTextView.setPadding(8, 8, 8, 8)

        // Create and configure the location TextView
        val locationTextView = TextView(this)
        locationTextView.text = record.location
        locationTextView.setPadding(8, 8, 8, 8)

        // Create and configure the amount TextView
        val amountTextView = TextView(this)
        amountTextView.text = record.amount
        amountTextView.setPadding(8, 8, 8, 8)

        // Create and configure the date TextView
        val dateTextView = TextView(this)
        dateTextView.text = record.date
        dateTextView.setPadding(8, 8, 8, 8)

        // Add TextViews to the row
        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)
        tableRow.addView(dateTextView)

        // Add the row to the table layout
        tableLayout.addView(tableRow)
    }

    // Function to save a record to the Room database
    private fun saveRecord(record: Record) {
        lifecycleScope.launch {
            database.recordDao().insert(record)
        }
    }

    // Function to load records from the Room database on app startup
    private fun loadRecords() {
        lifecycleScope.launch {
            val records = database.recordDao().getAllRecords()
            records.forEach { addRecord(it) }
        }
    }
}
