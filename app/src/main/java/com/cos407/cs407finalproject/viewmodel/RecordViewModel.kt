package com.cos407.cs407finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.repository.RecordRepository
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    // Save a record using Repository
    fun saveRecord(record: Record) {
        viewModelScope.launch {
            try {
                repository.saveRecord(record)
            } catch (e: Exception) {
                // Log error or notify the user (optional)
                e.printStackTrace()
            }
        }
    }

    // Synchronize records between Firebase and local Room database
    fun syncRecords(userId: Int) {
        viewModelScope.launch {
            try {
                repository.syncRecords(userId) // This should not have a return value
            } catch (e: Exception) {
                // Log error or notify the user (optional)
                e.printStackTrace()
            }
        }
    }

    // Delete function
    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    // Update function
    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    // Get all records for a specific user
    fun getRecords(userId: Int, onResult: (List<Record>) -> Unit) {
        viewModelScope.launch {
            try {
                val records = repository.getRecordsByUser(userId)
                onResult(records) // Pass the records to the callback
            } catch (e: Exception) {
                // Handle error (optional)
                e.printStackTrace()
                onResult(emptyList()) // Return an empty list on error
            }
        }
    }
}
