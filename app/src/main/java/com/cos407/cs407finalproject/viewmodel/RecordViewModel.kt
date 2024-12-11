package com.cos407.cs407finalproject.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    fun saveRecord(record: Record, callback: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    repository.saveRecord(record)
                }
                withContext(Dispatchers.Main) {
                    callback(id)
                }
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Error saving record", e)
                callback(-1)
            }
        }
    }

    fun syncRecords(userId: Int) {
        viewModelScope.launch {
            repository.syncRecords(userId)
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun getRecords(userId: Int, onResult: (List<Record>) -> Unit) {
        viewModelScope.launch {
            val records = repository.getRecordsByUser(userId)
            onResult(records)
        }
    }

}