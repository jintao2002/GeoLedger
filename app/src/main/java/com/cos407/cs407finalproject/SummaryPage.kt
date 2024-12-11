package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.repository.RecordRepository
import com.cos407.cs407finalproject.viewmodel.RecordViewModel
import com.cos407.cs407finalproject.viewmodel.RecordViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class SummaryPage : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var db: FirebaseFirestore
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_page) // Ensure this layout file matches your XML file name

        // Initialize chart
        barChart = findViewById(R.id.expenseChart)
        setupChart()

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize ViewModel
        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        val factory = RecordViewModelFactory(application)
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        // Update chart data
        CoroutineScope(Dispatchers.Main).launch {
            updateChartData()
        }

        // Navigation buttons
        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val btnSummary = findViewById<Button>(R.id.btnSummary)
        val btnMe = findViewById<Button>(R.id.btnMe)

        btnRecord.setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        btnSummary.setOnClickListener {
            // Refresh chart data if needed
            CoroutineScope(Dispatchers.Main).launch {
                updateChartData()
            }
        }

        btnMe.setOnClickListener {
            val intent = Intent(this, MePage::class.java)
            startActivity(intent)
        }
    }

    private fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            legend.isEnabled = true
        }

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        barChart.axisLeft.apply {
            setDrawGridLines(true)
            setDrawAxisLine(true)
            axisMinimum = 0f
        }

        barChart.axisRight.isEnabled = false
    }

    private suspend fun updateChartData() {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val userId = getCurrentUserId()

        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dailyExpenses = withContext(Dispatchers.IO) {
                recordViewModel.getDailyExpenses(userId, startOfDay, endOfDay)
            }
            val expenseAmount = dailyExpenses.values.firstOrNull()?.toFloat() ?: 0f

            entries.add(BarEntry((6 - i).toFloat(), expenseAmount))

            val dateStr = android.text.format.DateFormat.format("MM/dd", calendar.time) as String
            labels.add(dateStr)
        }

        val dataSet = BarDataSet(entries, "Daily Expenses")
        dataSet.color = resources.getColor(R.color.black)

        val barData = BarData(dataSet)
        barData.barWidth = 0.85f

        // Set X axis labels
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        barChart.data = barData
        barChart.invalidate()
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }
}