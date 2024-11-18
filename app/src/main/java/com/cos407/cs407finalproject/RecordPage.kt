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

        // Load saved records for the current user
        loadRecords()

        // FloatingActionButton to open the add record dialog
        findViewById<FloatingActionButton>(R.id.fabAddRecord).setOnClickListener {
            showAddRecordDialog()
        }

        // Record button: opens input dialog for new record
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            showAddRecordDialog()
        }

        // Me button: Go to the Me page
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            val intent = Intent(this, MePage::class.java)
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

        alertDialog = AlertDialog.Builder(this).setTitle("Add Record").setView(dialogView).create()
        alertDialog?.show()
    }

    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
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

        val itemTextView = TextView(this)
        itemTextView.text = record.item
        itemTextView.setPadding(22, 8, 8, 8)

        val locationTextView = TextView(this)
        locationTextView.text = record.location
        locationTextView.setPadding(22, 8, 8, 8)

        val amountTextView = TextView(this)
        amountTextView.text = "$${record.amount}"
        amountTextView.setPadding(22, 8, 8, 8)

        val dateTextView = TextView(this)
        dateTextView.text = record.date
        dateTextView.setPadding(22, 8, 8, 8)

        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)
        tableRow.addView(dateTextView)

        tableRow.setOnLongClickListener {
            showEditDeleteDialog(record, tableRow)
            true
        }

        tableLayout.addView(tableRow)
    }

    private fun showEditDeleteDialog(record: Record, tableRow: TableRow) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this).setTitle("Select an Action").setItems(options) { _, which ->
                when (which) {
                    0 -> showAddRecordDialog(record, tableRow)
                    1 -> deleteRecord(record, tableRow)
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun deleteRecord(record: Record, tableRow: TableRow) {
        lifecycleScope.launch {
            database.recordDao().delete(record)
            tableLayout.removeView(tableRow)
        }
    }

    private fun saveRecord(record: Record) {
        lifecycleScope.launch {
            database.recordDao().insert(record)
        }
    }

    private fun updateRecord(record: Record) {
        lifecycleScope.launch {
            database.recordDao().update(record)
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

    private fun loadRecords() {
        lifecycleScope.launch {
            val userId = getCurrentUserId()
            val records = database.recordDao().getAllRecordsByUser(userId)
            records.forEach { addRecord(it) }
        }
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }
}
