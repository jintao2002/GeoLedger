package com.cos407.cs407finalproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.cos407.cs407finalproject.database.AppDatabase
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.repository.RecordRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao = AppDatabase.getDatabase(application).recordDao()
    private val repository = RecordRepository(recordDao)
    private val firebaseDb = FirebaseFirestore.getInstance()

    fun saveRecord(record: Record, callback: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            try {

                val id = repository.saveRecord(record)

                val userRecord = hashMapOf(
                    "userId" to record.userId,
                    "item" to record.item,
                    "amount" to record.amount,
                    "category" to record.category,
                    "location" to record.location,
                    "date" to record.date
                )

                firebaseDb.collection("records")
                    .add(userRecord)
                    .addOnSuccessListener { documentReference ->
                        println("DocumentSnapshot added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Error adding document: $e")
                    }


                callback?.invoke(id)
            } catch (e: Exception) {
                println("Error saving record: ${e.message}")
            }
        }
    }

    fun getRecords(userId: Int, callback: (List<Record>) -> Unit) {
        viewModelScope.launch {
            val records = repository.getRecordsByUser(userId)
            callback(records)
        }
    }

    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun syncRecords(userId: Int) {
        viewModelScope.launch {
            repository.syncRecords(userId)
        }
    }

    fun getDailyExpenses(userId: Int, startDate: Long, endDate: Long) = liveData(Dispatchers.IO) {
        val dailyExpenses = repository.getDailyExpenseSync(userId, startDate, endDate)
        emit(dailyExpenses)
    }

    suspend fun getMonthlyStatistics(
        userId: Int,
        year: Int,
        month: Int
    ) = repository.getMonthlyStatistics(userId, year, month)
}