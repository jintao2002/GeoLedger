package com.cos407.cs407finalproject

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
import java.util.Date
import java.util.Locale

class RecordPage : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private var selectedLocation: String = "N/A"
    private var selectedDate: String = "N/A"
    private var selectedCategory: String = "Uncategorized"
    private var currentDialogView: View? = null
    private var alertDialog: AlertDialog? = null

    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        val factory = RecordViewModelFactory(repository) // Now using repository in factory
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        tableLayout = findViewById(R.id.tableLayout)

        val apiKey = BuildConfig.MAPS_API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d("PlacesAPI", "Google Places API initialized")
        }

        loadRecords()

        findViewById<FloatingActionButton>(R.id.fabAddRecord).setOnClickListener {
            showAddRecordDialog()
        }

        findViewById<Button>(R.id.btnSummary).setOnClickListener {
            val intent = Intent(this, SummaryPage::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnMe).setOnClickListener {
            val intent = Intent(this, MePage::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageView>(R.id.myProfilePhoto)
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
        val categoryGrid = dialogView.findViewById<GridLayout>(R.id.categoryGrid)
        setupCategoryButtons(categoryGrid)

        if (existingRecord != null) {
            etItem.setText(existingRecord.item)
            etAmount.setText(existingRecord.amount.toString())
            tvLocation.text = existingRecord.location

            val dateFormat = java.text.SimpleDateFormat("MM-dd", Locale.getDefault())
            val date = Date(existingRecord.date)
            tvDate.text = dateFormat.format(date)
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
            val amountStr = etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0

            // If no date selected, use current time
            val chosenDateStr = tvDate.text.toString()
            val recordTimestamp = if (chosenDateStr == "N/A") {
                System.currentTimeMillis()
            } else {
                // parse yyyy-MM-dd format from openDatePicker
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = sdf.parse(chosenDateStr)
                parsedDate?.time ?: System.currentTimeMillis()
            }

            val record = Record(
                id = existingRecord?.id ?: 0,
                amount = amount,
                category = selectedCategory,
                item = item,
                location = tvLocation.text.toString(),
                date = recordTimestamp,
                userId = getCurrentUserId()
            )

            if (existingRecord != null) {
                val updatedRecord = record.copy(id = existingRecord.id)
                recordViewModel.updateRecord(updatedRecord)
                tableRow?.let { updateTableRow(updatedRecord, it) }
            } else {
                recordViewModel.saveRecord(record) { newId ->
                    runOnUiThread {
                        val completeRecord = record.copy(id = newId.toInt())
                        // Immediately display the new record
                        addRecord(completeRecord)
                        tableLayout.requestLayout()
                    }
                }
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
                val formattedDate = String.format("%d-%02d-%02d", year, month+1, dayOfMonth)
                tvDate.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun addRecord(record: Record) {
        val tableRow = TableRow(this)
        tableRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )

        val itemTextView = TextView(this)
        itemTextView.text = record.item
        itemTextView.setPadding(16, 8, 8, 8)

        val locationTextView = TextView(this)
        locationTextView.text = record.location
        locationTextView.setPadding(16, 8, 8, 8)

        val amountTextView = TextView(this)
        amountTextView.text = "$${record.amount}"
        amountTextView.setPadding(16, 8, 8, 8)

        val dateTextView = TextView(this)
        val dateFormat = java.text.SimpleDateFormat("MM-dd", Locale.getDefault())
        dateTextView.text = dateFormat.format(Date(record.date))
        dateTextView.setPadding(16, 8, 8, 8)

        val actionsLayout = LinearLayout(this)
        actionsLayout.orientation = LinearLayout.HORIZONTAL
        actionsLayout.setPadding(8, 8, 8, 8)

        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)
        tableRow.addView(dateTextView)
        tableRow.addView(actionsLayout)

        tableRow.setOnLongClickListener {
            showEditDeleteDialog(record, tableRow)
            true
        }

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
            recordViewModel.deleteRecord(record)
            runOnUiThread {
                tableLayout.removeView(tableRow)
            }
            loadRecords()
        } catch (e: Exception) {
            Log.e("RecordPage", "Error deleting record", e)
            Toast.makeText(this, "Error deleting record", Toast.LENGTH_SHORT).show()
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
        dateTextView.text = java.text.SimpleDateFormat("MM-dd", Locale.getDefault())
            .format(Date(record.date))
    }

    private fun loadRecords() {
        val userId = getCurrentUserId()
        tableLayout.removeAllViews()

        recordViewModel.getRecords(userId) { records ->
            runOnUiThread {
                tableLayout.removeAllViews()
                records.forEach { addRecord(it) }
            }
        }
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }

    private fun setupCategoryButtons(gridLayout: GridLayout) {
        val originalBackground = ResourcesCompat.getDrawable(resources, R.drawable.category_button_bg, null)
        val selectedBackground = ResourcesCompat.getDrawable(resources, R.drawable.category_button_selected_bg, null)

        val categoryButtonMap = mapOf(
            R.id.btnRestaurant to "Restaurant",
            R.id.btnTransport to "Transportation",
            R.id.btnShopping to "Shopping",
            R.id.btnGrocery to "Grocery",
            R.id.btnPower to "Power",
            R.id.btnEducation to "Education",
            R.id.btnSnack to "Snack",
            R.id.btnCloths to "Clothes",
            R.id.btnFurniture to "Furniture",
            R.id.btnFitness to "Fitness",
            R.id.btnCommunication to "Communication",
            R.id.btnTravel to "Travel",
            R.id.btnGift to "Gift",
            R.id.btnGames to "Games",
            R.id.btnPets to "Pets",
            R.id.btnMedicals to "Medical"
        )

        var currentlySelectedButton: Button? = null

        categoryButtonMap.forEach { (buttonId, category) ->
            gridLayout.findViewById<Button>(buttonId)?.apply {
                setOnClickListener {
                    currentlySelectedButton?.background = originalBackground
                    currentlySelectedButton = this
                    background = selectedBackground
                    selectedCategory = category
                }
            }
        }
    }
}
