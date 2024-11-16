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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.Record
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar


class RecordPage : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private var selectedLocation: String = "N/A" // Store selected location
    private var selectedDate: String = "N/A" // Store selected date
    private var currentDialogView: View? = null // Reference to the current dialog view
    private var alertDialog: AlertDialog? = null
    private lateinit var database: AppDatabase // Database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Initialize tableLayout after setting content view
        tableLayout = findViewById(R.id.tableLayout)

        // Initialize the Google Places API
        val apiKey = BuildConfig.MAPS_API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d("PlacesAPI", "Google Places API initialized")
        }

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
    // share the same dialog with editing mode
    private fun showAddRecordDialog(existingRecord: Record? = null, tableRow: TableRow? = null) {
        Log.d("DEBUG", "Opening dialog. Existing record: $existingRecord, TableRow: $tableRow")
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)
        currentDialogView = dialogView  // Store reference to the dialog view

        val etItem = dialogView.findViewById<EditText>(R.id.etItem)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvLocation = dialogView.findViewById<TextView>(R.id.tvLocation)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        val btnAction = dialogView.findViewById<Button>(R.id.btnAddRecord)

        //switching between editing and adding modes
        if (existingRecord != null) {
            // fill in with existing data
            etItem.setText(existingRecord.item)
            etAmount.setText(existingRecord.amount.replace("$", ""))
            tvLocation.text = existingRecord.location
            tvDate.text = existingRecord.date
            btnAction.text = "Update"
        } else {
            // Set initial text for location and date fields
            tvLocation.text = selectedLocation
            tvDate.text = selectedDate
            btnAction.text = "Add"
        }


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
                id = existingRecord?.id ?: 0,
                amount = amount.toString(),
                item = item.toString(),
                location = tvLocation.text.toString(),
                date = tvDate.text.toString()
            )

            if(existingRecord != null) {
                updateRecord(record) // Update data
                tableRow?.let { updateTableRow(record, it) } // Update tableRow
            } else {
                saveRecord(record) // Save the record to the database
                addRecord(record) // Add record to the table layout
            }
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

    // ActivityResultLauncher
    private val autocompleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            selectedLocation = place.name ?: "Unknown location"
            // Update the position text in the dialog box
            currentDialogView?.findViewById<TextView>(R.id.tvLocation)?.text = selectedLocation
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(result.data!!)
            Log.e("AutocompleteError", status.statusMessage ?: "Unknown error")
        }
    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setCountry("US")  // Country can be set as desired
            .build(this)
        autocompleteLauncher.launch(intent)
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
        itemTextView.setPadding(22, 8, 8, 8)

        // Create and configure the location TextView
        val locationTextView = TextView(this)
        locationTextView.text = record.location
        locationTextView.setPadding(22, 8, 8, 8)

        // Create and configure the amount TextView
        val amountTextView = TextView(this)
        amountTextView.text = "$${record.amount}"
        amountTextView.setPadding(22, 8, 8, 8)

        // Create and configure the date TextView
        val dateTextView = TextView(this)
        dateTextView.text = record.date
        dateTextView.setPadding(22, 8, 8, 8)

        // Add TextViews to the row
        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)
        tableRow.addView(dateTextView)

        //show dialog when long pressing
        tableRow.setOnLongClickListener {
            showEditDeleteDialog(record, tableRow)
            true
        }

        // Add the row to the table layout
        tableLayout.addView(tableRow)
    }

    //dialog for editing and deleting records
    private fun showEditDeleteDialog(record: Record, tableRow: TableRow) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Select an Action")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { val updatedRecord = Record(
                        id = record.id,
                        item = (tableRow.getChildAt(0) as TextView).text.toString(),
                        location = (tableRow.getChildAt(1) as TextView).text.toString(),
                        amount = (tableRow.getChildAt(2) as TextView).text.toString(),
                        date = (tableRow.getChildAt(3) as TextView).text.toString()
                    )
                        showAddRecordDialog(updatedRecord, tableRow)
                    } // edit
                    1 -> deleteRecord(record, tableRow) // delete
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to delete a record
    private fun deleteRecord(record: Record, tableRow: TableRow) {
        lifecycleScope.launch {
            // delete data
            database.recordDao().delete(record)
            // remove tableview
            tableLayout.removeView(tableRow)
        }
    }

    // Function to save a record to the Room database
    private fun saveRecord(record: Record) {
        lifecycleScope.launch {
            database.recordDao().insert(record)
        }
    }

    //update record
    private fun updateRecord(record: Record) {
        lifecycleScope.launch {
            database.recordDao().update(record)
            Log.d("DEBUG", "Updated record in database: $record")

        }
    }
    private fun updateTableRow(record: Record, tableRow: TableRow) {

        val itemTextView = tableRow.getChildAt(0) as TextView
        val locationTextView = tableRow.getChildAt(1) as TextView
        val amountTextView = tableRow.getChildAt(2) as TextView
        val dateTextView = tableRow.getChildAt(3) as TextView

        itemTextView.text = record.item
        locationTextView.text = record.location
        amountTextView.text = "$${record.amount}"
        dateTextView.text = record.date
    }

    // Function to load records from the Room database on app startup
    private fun loadRecords() {
        lifecycleScope.launch {
            val records = database.recordDao().getAllRecords()
            records.forEach { addRecord(it) }
        }
    }
}
