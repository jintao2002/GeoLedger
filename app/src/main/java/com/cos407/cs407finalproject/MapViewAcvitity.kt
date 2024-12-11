package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.repository.RecordRepository
import com.cos407.cs407finalproject.viewmodel.RecordViewModel
import com.cos407.cs407finalproject.viewmodel.RecordViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var recordViewModel: RecordViewModel

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_page)

        // get permission
        checkAndRequestPermissions()

        initViewModel()
        setupNavigationButtons()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: Log.e("MapViewActivity", "Failed to get map fragment")
    }

    private fun initViewModel() {
        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        val factory = RecordViewModelFactory(repository)
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                initMap()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            startActivity(Intent(this, RecordPage::class.java))
        }
        findViewById<Button>(R.id.btnSummary).setOnClickListener {
            startActivity(Intent(this, SummaryPage::class.java))
        }
        findViewById<Button>(R.id.btnMe).setOnClickListener {
            startActivity(Intent(this, MePage::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("MapDebug", "Map is ready")

        loadRecordsAndAddMarkers()

    }

    private fun loadRecordsAndAddMarkers() {
        val userId = getCurrentUserId()
        recordViewModel.getRecords(userId) { records ->
            records.forEach { record ->
                Log.d("MapDebug", "Processing record at location: ${record.location}")

                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(record.location, 1)
                    Log.d("MapDebug", "Geocoder found ${addresses?.size ?: 0} addresses")


                    addresses?.firstOrNull()?.let { address ->
                        val location = LatLng(address.latitude, address.longitude)
                        Log.d("MapDebug", "Adding marker at: ${location.latitude}, ${location.longitude}")

                        mMap.addMarker(MarkerOptions().apply {
                            position(location)
                            title("${record.category}: ${record.item}")
                            snippet("$${record.amount} at ${record.location}")
                        })

                        // move camera to the latest position
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    }
                } catch (e: Exception) {
                    Log.e("MapView", "Error geocoding location: ${record.location}", e)
                }
            }
        }
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }
}