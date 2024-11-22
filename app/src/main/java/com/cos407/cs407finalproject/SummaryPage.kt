package com.cos407.cs407finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import java.util.*
import kotlin.collections.ArrayList

class SummaryPage : AppCompatActivity() {

    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_page) // Ensure this layout file matches your XML file name

        // Initialize chart
        barChart = findViewById(R.id.expenseChart)
        setupChart()
        updateChartData()

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
            updateChartData()
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

    private fun updateChartData() {
        // TODO replace with actual data from your database
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Get last 7 days of expenses
        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time

            // TODO Replace with actual data from database
            val expenseAmount = getDailyExpense(date)
            entries.add(BarEntry((6 - i).toFloat(), expenseAmount))

            // Format date
            val dateStr = android.text.format.DateFormat.format("MM/dd", date) as String
            labels.add(dateStr)
        }

        val dataSet = BarDataSet(entries, "Daily Expenses")
        dataSet.color = resources.getColor(R.color.black) // Define this color in colors.xml

        val barData = BarData(dataSet)
        barData.barWidth = 0.85f

        // Set X axis labels
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        barChart.data = barData
        barChart.invalidate()
    }

    // Placeholder method - implement to get actual expense data
    private fun getDailyExpense(date: Date): Float {
        // TODO: Replace with actual database query
        //return random sample data
        return (Math.random() * 100).toFloat()
    }
}