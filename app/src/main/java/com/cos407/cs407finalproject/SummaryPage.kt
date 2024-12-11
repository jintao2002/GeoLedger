package com.cos407.cs407finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.repository.MonthlyStatistics
import com.cos407.cs407finalproject.repository.RecordRepository
import com.cos407.cs407finalproject.viewmodel.RecordViewModel
import com.cos407.cs407finalproject.viewmodel.RecordViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SummaryPage : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var monthTextView: TextView
    private lateinit var totalExpenseTextView: TextView
    private lateinit var dailyAverageTextView: TextView
    private var currentMonth: Calendar = Calendar.getInstance()

    // Flag indicating if we are currently showing the PieChart instead of the BarChart
    private var showPieChart = false

    // Cached variables to avoid unnecessary recalculations
    private var cachedUserId: Int? = null
    private var cachedYear: Int? = null
    private var cachedMonth: Int? = null
    private var cachedStatistics: MonthlyStatistics? = null

    companion object {
        // Key used in SharedPreferences to indicate if data is dirty (new records inserted)
        private const val DATA_DIRTY_KEY = "DATA_DIRTY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_page)

        barChart = findViewById(R.id.expenseChart)
        pieChart = findViewById(R.id.categoryPieChart)
        setupChart()

        val recordDao = AppDatabase.getDatabase(applicationContext).recordDao()
        val repository = RecordRepository(recordDao)
        val factory = RecordViewModelFactory(repository)
        recordViewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]

        monthTextView = findViewById(R.id.month)
        totalExpenseTextView = findViewById(R.id.totalExpense)
        dailyAverageTextView = findViewById(R.id.averageExpense)
        updateMonthTextView()

        // Load data when the page is first created
        CoroutineScope(Dispatchers.Main).launch {
            loadAndDisplayDataIfNeeded()
        }

        // Buttons for navigation and actions
        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val btnSummary = findViewById<Button>(R.id.btnSummary)
        val btnMe = findViewById<Button>(R.id.btnMe)
        val btnPrevious = findViewById<Button>(R.id.previous)
        val btnNext = findViewById<Button>(R.id.next)
        val btnToggleChart = findViewById<Button>(R.id.btnToggleChart)

        btnRecord.setOnClickListener {
            val intent = Intent(this, RecordPage::class.java)
            startActivity(intent)
        }

        btnSummary.setOnClickListener {
            // Force recalculation by setting data dirty flag
            CoroutineScope(Dispatchers.Main).launch {
                setDataDirtyFlag(true)
                loadAndDisplayDataIfNeeded()
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
                loadAndDisplayDataIfNeeded()
            }
        }

        btnNext.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateMonthTextView()
            CoroutineScope(Dispatchers.Main).launch {
                loadAndDisplayDataIfNeeded()
            }
        }

        btnToggleChart.setOnClickListener {
            // Toggle between BarChart and PieChart
            showPieChart = !showPieChart
            btnToggleChart.text = if (showPieChart) "Show Bar Chart" else "Show Pie Chart"
            cachedStatistics?.let {
                displayStatistics(it, cachedYear!!, cachedMonth!!)
            }
        }
    }

    /**
     * Setup the chart properties for Bar and Pie charts
     */
    private fun setupChart() {
        // Bar chart settings
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

        // Pie chart settings
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.graphics.Color.WHITE)
            setUsePercentValues(true)
            legend.isEnabled = true
        }
    }

    /**
     * Load data from the local database if needed.
     * If data is marked dirty or the month changed, recalculate.
     * Otherwise, use cached data.
     */
    private suspend fun loadAndDisplayDataIfNeeded() {
        val userId = getCurrentUserId()
        val year = currentMonth.get(Calendar.YEAR)
        val month = currentMonth.get(Calendar.MONTH) + 1

        val dataDirty = getDataDirtyFlag()
        val monthChanged = (year != cachedYear || month != cachedMonth || userId != cachedUserId)

        if (!dataDirty && !monthChanged && cachedStatistics != null) {
            // Use cached data if no changes and same month
            Log.d("SummaryPage", "Using cached statistics")
            displayStatistics(cachedStatistics!!, year, month)
            return
        }

        // Data changed or month changed, recalculate from the database
        val stats = withContext(Dispatchers.IO) {
            recordViewModel.getMonthlyStatistics(userId, year, month)
        }

        // Update cache
        cachedUserId = userId
        cachedYear = year
        cachedMonth = month
        cachedStatistics = stats

        // Clear the dirty flag after recalculation
        setDataDirtyFlag(false)

        // Display the updated data
        displayStatistics(stats, year, month)
    }

    /**
     * Display the statistics either as a BarChart or a PieChart depending on the showPieChart flag.
     */
    private fun displayStatistics(stats: MonthlyStatistics, year: Int, month: Int) {
        val daysInMonth = getDaysInMonth(year, month)

        val totalExpense = stats.totalExpense.toFloat()
        totalExpenseTextView.text = String.format("$%.2f", totalExpense)
        val dailyAverage = if (daysInMonth > 0) totalExpense / daysInMonth else 0f
        dailyAverageTextView.text = String.format("$%.2f", dailyAverage)

        if (showPieChart) {
            // Show PieChart and hide BarChart
            barChart.visibility = View.GONE
            pieChart.visibility = View.VISIBLE
            displayPieChart(stats.categoryExpenses)
        } else {
            // Show BarChart and hide PieChart
            pieChart.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            displayBarChart(stats, year, month, daysInMonth)
        }
    }

    /**
     * Display the daily expenses as a bar chart.
     */
    private fun displayBarChart(stats: MonthlyStatistics, year: Int, month: Int, daysInMonth: Int) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)

        for (day in 1..daysInMonth) {
            val dateKey = String.format("%04d-%02d-%02d", year, month, day)
            val expense = stats.dailyExpenses[dateKey] ?: 0.0
            entries.add(BarEntry(day.toFloat(), expense.toFloat()))

            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = android.text.format.DateFormat.format("MM/dd", calendar.time) as String
            labels.add(dateStr)
        }

        val dataSet = BarDataSet(entries, "Daily Expenses")
        dataSet.color = resources.getColor(R.color.black)
        val barData = BarData(dataSet)
        barData.barWidth = 0.85f

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.data = barData
        barChart.invalidate()
    }

    /**
     * Display category expenses as a pie chart.
     * We have 16 categories, so we prepare 16 colors.
     */
    private fun displayPieChart(categoryExpenses: Map<String, Double>) {
        val entries = ArrayList<PieEntry>()
        for ((category, amount) in categoryExpenses) {
            if (amount > 0) {
                entries.add(PieEntry(amount.toFloat(), category))
            }
        }

        val dataSet = PieDataSet(entries, "Expenses by Category")
        // Create an array of 16 colors from the resources
        val colors = intArrayOf(
            R.color.colorCategory1,
            R.color.colorCategory2,
            R.color.colorCategory3,
            R.color.colorCategory4,
            R.color.colorCategory5,
            R.color.colorCategory6,
            R.color.colorCategory7,
            R.color.colorCategory8,
            R.color.colorCategory9,
            R.color.colorCategory10,
            R.color.colorCategory11,
            R.color.colorCategory12,
            R.color.colorCategory13,
            R.color.colorCategory14,
            R.color.colorCategory15,
            R.color.colorCategory16
        )
        dataSet.setColors(colors, this)

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(android.graphics.Color.WHITE)

        pieChart.data = pieData
        pieChart.invalidate()
    }

    /**
     * Get the number of days in the specified month and year.
     */
    private fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Update the month title text according to currentMonth.
     */
    private fun updateMonthTextView() {
        val dateFormat = SimpleDateFormat("MMM. yyyy", Locale.ENGLISH)
        val formattedDate = dateFormat.format(currentMonth.time)
        monthTextView.text = formattedDate
    }

    /**
     * Get the current user ID from SharedPreferences.
     */
    private fun getCurrentUserId(): Int {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("CURRENT_USER_ID", -1)
    }

    /**
     * Check if data is marked as dirty.
     */
    private fun getDataDirtyFlag(): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DATA_DIRTY_KEY, false)
    }

    /**
     * Set the data dirty flag. This should be set to true when new records are inserted in RecordPage.
     */
    private fun setDataDirtyFlag(value: Boolean) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(DATA_DIRTY_KEY, value).apply()
    }
}
