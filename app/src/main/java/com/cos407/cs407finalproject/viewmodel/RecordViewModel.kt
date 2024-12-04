package com.cos407.cs407finalproject.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.repository.RecordRepository
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    fun saveRecord(record: Record, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // get new id
                val newId = repository.saveRecord(record)
                // pass back id
                onSaved(newId)
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Error saving record", e)
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

//    suspend fun getDailyExpenseSync(date: String, userId: Int): Float? {
//        return repository.getDailyExpenseSync(date, userId)
//    }
}