package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
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
    private lateinit var monthTextView: TextView
    private lateinit var totalExpenseTextView: TextView
    private lateinit var dailyAverageTextView: TextView
    private var currentMonth: Calendar = Calendar.getInstance()

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

        // Initialize month and total expense TextViews
        monthTextView = findViewById(R.id.month)
        totalExpenseTextView = findViewById(R.id.totalExpense)
        dailyAverageTextView = findViewById(R.id.averageExpense)
        updateMonthTextView()

        // Update chart data
        CoroutineScope(Dispatchers.Main).launch {
            updateChartData()
        }

        // Navigation buttons
        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val btnSummary = findViewById<Button>(R.id.btnSummary)
        val btnMe = findViewById<Button>(R.id.btnMe)
        val btnPrevious = findViewById<Button>(R.id.previous)
        val btnNext = findViewById<Button>(R.id.next)

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

        btnPrevious.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateMonthTextView()
            CoroutineScope(Dispatchers.Main).launch {
                updateChartData()
            }
        }

        btnNext.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateMonthTextView()
            CoroutineScope(Dispatchers.Main).launch {
                updateChartData()
            }
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
        var totalExpense = 0f

        val userId = getCurrentUserId()
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..daysInMonth) {
            val calendar = currentMonth.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, i)
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

            val expenseAmount = withContext(Dispatchers.IO) { getDailyExpenseFromFirebase(userId, startOfDay, endOfDay) }
            totalExpense += expenseAmount

            entries.add(BarEntry(i.toFloat(), expenseAmount))

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

        // Update total expense TextView
        totalExpenseTextView.text = String.format("$%.2f", totalExpense)

        // Calculate and update daily average TextView
        val dailyAverage = if (daysInMonth > 0) totalExpense / daysInMonth else 0f
        dailyAverageTextView.text = String.format("$%.2f", dailyAverage)
    }

    private suspend fun getDailyExpenseFromFirebase(userId: Int, startDate: Long, endDate: Long): Float {
        var totalExpense = 0f

        try {
            val records = db.collection("records")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            for (document in records) {
                val amount = document.getDouble("amount") ?: 0.0
                totalExpense += amount.toFloat()
            }
        } catch (e: Exception) {
            Log.e("SummaryPage", "Error getting daily expense", e)
        }
        return totalExpense
    }

    private fun updateMonthTextView() {
        val monthFormat = android.text.format.DateFormat.format("MMMM yyyy", currentMonth.time) as String
        monthTextView.text = monthFormat
    }

    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }
}