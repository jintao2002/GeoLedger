package com.cos407.cs407finalproject

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.repository.RecordRepository
import com.cos407.cs407finalproject.viewmodel.RecordViewModel
import com.cos407.cs407finalproject.viewmodel.RecordViewModelFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class RecordPage : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private var selectedLocation: String = "N/A" // Store selected location
    private var selectedDate: String = "N/A" // Store selected date
    private var currentDialogView: View? = null // Reference to the current dialog view
    private var alertDialog: AlertDialog? = null

    // Initialize the RecordViewModel
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Initialize the RecordViewModel using the Factory
        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        val factory = RecordViewModelFactory(repository)
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        // Initialize tableLayout after setting content view
        tableLayout = findViewById(R.id.tableLayout)

        // Initialize the Google Places API
        val apiKey = BuildConfig.MAPS_API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d("PlacesAPI", "Google Places API initialized")
        }

        // Load saved records for the current user
        loadRecords()

        // FloatingActionButton to open the add record dialog
        findViewById<FloatingActionButton>(R.id.fabAddRecord).setOnClickListener {
            showAddRecordDialog()
        }

        // Summary button: opens input dialog for new record
        findViewById<Button>(R.id.btnSummary).setOnClickListener {
            val intent = Intent(this, SummaryPage::class.java)
            startActivity(intent)
        }

        // Me button: Go to the Me page
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            val intent = Intent(this, MePage::class.java)
            startActivity(intent)
        }

        // Find the button that leads to slide.
        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)

        // Set up the intent, from profile photo to slide menu
        profileButton.setOnClickListener {
            val intent = Intent(this, SlideActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAddRecordDialog(existingRecord: Record? = null, tableRow: TableRow? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)
        currentDialogView = dialogView

        val etItem = dialogView.findViewById<EditText>(R.id.etItem)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvLocation = dialogView.findViewById<TextView>(R.id.tvLocation)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        val btnAction = dialogView.findViewById<Button>(R.id.btnAddRecord)

        if (existingRecord != null) {
            etItem.setText(existingRecord.item)
            etAmount.setText(existingRecord.amount.replace("$", ""))
            tvLocation.text = existingRecord.location
            tvDate.text = existingRecord.date
            btnAction.text = "Update"
        } else {
            tvLocation.text = selectedLocation
            tvDate.text = selectedDate
            btnAction.text = "Add"
        }

        dialogView.findViewById<Button>(R.id.btnSelectLocation).setOnClickListener {
            openPlacePicker()
        }

        dialogView.findViewById<Button>(R.id.btnSelectDate).setOnClickListener {
            openDatePicker(tvDate)
        }

        dialogView.findViewById<Button>(R.id.btnAddRecord).setOnClickListener {
            val item = etItem.text.toString()
            val amount = etAmount.text.toString()

            val record = Record(
                id = existingRecord?.id ?: 0,
                amount = amount,
                item = item,
                location = tvLocation.text.toString(),
                date = tvDate.text.toString(),
                userId = getCurrentUserId() // Assign the current user's ID
            )

            if (existingRecord != null) {
                updateRecord(record)
                tableRow?.let { updateTableRow(record, it) }
            } else {
                saveRecord(record)
                addRecord(record)
            }

            currentDialogView = null
            alertDialog?.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            currentDialogView = null
            alertDialog?.dismiss()
        }

        alertDialog = AlertDialog.Builder(this)
            .setTitle(if (existingRecord != null) "Edit Record" else "Add Record")
            .setView(dialogView).create()
        alertDialog?.show()
    }

    private fun openPlacePicker() {
        val fields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG
        )
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("US")
                .build(this)
        autocompleteLauncher.launch(intent)
    }

    private val autocompleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            selectedLocation = place.name ?: "Unknown location"
            currentDialogView?.findViewById<TextView>(R.id.tvLocation)?.text = selectedLocation
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(result.data!!)
            Log.e("AutocompleteError", status.statusMessage ?: "Unknown error")
        }
    }

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

    fun addRecord(record: Record) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        // Item TextView
        val itemTextView = TextView(this)
        itemTextView.text = record.item
        itemTextView.setPadding(16, 8, 8, 8)

        // Location TextView
        val locationTextView = TextView(this)
        locationTextView.text = record.location
        locationTextView.setPadding(16, 8, 8, 8)

        // Amount TextView
        val amountTextView = TextView(this)
        amountTextView.text = "$${record.amount}"
        amountTextView.setPadding(16, 8, 8, 8)

        // Date TextView
        val dateTextView = TextView(this)
        dateTextView.text = record.date
        dateTextView.setPadding(16, 8, 8, 8)

        // Actions Layout
        val actionsLayout = LinearLayout(this)
        actionsLayout.orientation = LinearLayout.HORIZONTAL
        actionsLayout.setPadding(8, 8, 8, 8)

        // Edit Button
        val editButton = Button(this)
        editButton.text = "Edit"
        editButton.textSize = 10f
        editButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
        editButton.setTextColor(resources.getColor(android.R.color.white))
        val editParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editParams.marginEnd = 4
        editButton.layoutParams = editParams

        // Delete Button
        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        deleteButton.textSize = 10f
        deleteButton.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
        deleteButton.setTextColor(resources.getColor(android.R.color.white))
        deleteButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Add buttons to actions layout
        actionsLayout.addView(editButton)
        actionsLayout.addView(deleteButton)

        // Add all views to the table row
        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)
        tableRow.addView(dateTextView)
        tableRow.addView(actionsLayout)

        // Set click listeners for buttons
        editButton.setOnClickListener {
            showAddRecordDialog(record, tableRow)
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(record, tableRow)
        }

        // Add the row to the table
        tableLayout.addView(tableRow)
    }

    private fun showEditDeleteDialog(record: Record, tableRow: TableRow) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Select an Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddRecordDialog(record, tableRow)
                    1 -> showDeleteConfirmationDialog(record, tableRow)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(record: Record, tableRow: TableRow) {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this record?\n\nItem: ${record.item}\nAmount: $${record.amount}")
            .setPositiveButton("Delete") { _, _ ->
                deleteRecord(record, tableRow)
                Toast.makeText(this, "Record deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRecord(record: Record, tableRow: TableRow) {
        try {
            // Delete from database using ViewModel
            recordViewModel.deleteRecord(record)

            // Remove from UI
            tableLayout.removeView(tableRow)

            // Sync with Firebase if needed
            val userId = getCurrentUserId()
            recordViewModel.syncRecords(userId)

        } catch (e: Exception) {
            Log.e("RecordPage", "Error deleting record", e)
            Toast.makeText(this, "Error deleting record", Toast.LENGTH_SHORT).show()
        }
    }

    private fun swipeToDelete(tableRow: TableRow, record: Record) {
        // Remove the row with animation
        tableRow.animate()
            .translationX(tableRow.width.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                deleteRecord(record, tableRow)
            }
    }

    fun deleteRecord(record: Record) {
        //TODO delete from databases
    }

    private fun saveRecord(record: Record) {
        // Use the ViewModel to save the record
        recordViewModel.saveRecord(record)
    }

    private fun updateRecord(record: Record) {
        // Use the ViewModel to update the record
        recordViewModel.updateRecord(record)
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

    private fun loadRecords() {
        val userId = getCurrentUserId()
        // Use the ViewModel to get records
        recordViewModel.getRecords(userId) { records ->
            records.forEach { addRecord(it) }
        }

        // Optionally sync records from Firebase
        recordViewModel.syncRecords(userId)
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }
}
