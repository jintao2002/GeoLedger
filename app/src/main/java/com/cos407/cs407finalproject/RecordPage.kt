package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cos407.cs407finalproject.database.Record
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class RecordPage : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private val REQUEST_CODE_PLACE_PICKER = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_page)

        // Initialize tableLayout after setting content view
        tableLayout = findViewById(R.id.tableLayout)

        // Initialize the Google Places API with your API key
        Places.initialize(applicationContext, "AIzaSyAKePaqK9_QlRkYY4gf4VdP2zwLiqobyic")  //API Key
        val placesClient = Places.createClient(this)

        // Record button: opens place picker
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            openPlacePicker()
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

    // Function to open the Google Places picker
    private fun openPlacePicker() {
        val fields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startActivityForResult(intent, REQUEST_CODE_PLACE_PICKER)
    }

    // Handle the result from the place picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PLACE_PICKER && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val locationName = place.name ?: "Unknown location"

            // Use the selected location name as the location field in the record
            val record = Record(amount = "0", item = "New Item", location = locationName)
            addRecord(record)
        }
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

        // Add TextViews to the row
        tableRow.addView(itemTextView)
        tableRow.addView(locationTextView)
        tableRow.addView(amountTextView)

        // Add the row to the table layout
        tableLayout.addView(tableRow)
    }
}
