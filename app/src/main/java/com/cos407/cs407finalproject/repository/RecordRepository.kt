package com.cos407.cs407finalproject.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.database.RecordDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecordRepository(private val recordDao: RecordDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()

    suspend fun saveRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {

                recordDao.insert(record)

                val documentRef = firebaseDb.collection("records").add(record).await()
                Log.d("RecordRepository", "Record saved with ID: ${documentRef.id}")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Error saving record", e)
            }
        }
    }

    suspend fun syncRecords(userId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val documents = firebaseDb.collection("records")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                for (document in documents) {
                    val record = document.toObject(Record::class.java)
                    recordDao.insert(record)
                }
                Log.d("RecordRepository", "Sync completed for user $userId")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Sync failed", e)
            }
        }
    }

    suspend fun deleteRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {

                recordDao.delete(record)


                val querySnapshot = firebaseDb.collection("records")
                    .whereEqualTo("id", record.id)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.reference.delete().await()
                }
                Log.d("RecordRepository", "Record deleted successfully")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Error deleting record", e)
            }
        }
    }

    suspend fun updateRecord(record: Record) {
        withContext(Dispatchers.IO) {
            try {

                recordDao.update(record)


                val querySnapshot = firebaseDb.collection("records")
                    .whereEqualTo("id", record.id)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    document.reference.set(record).await()
                }
                Log.d("RecordRepository", "Record updated successfully")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Error updating record", e)
            }
        }
    }

    suspend fun getRecordsByUser(userId: Int): List<Record> {
        return withContext(Dispatchers.IO) {
            recordDao.getAllRecordsByUser(userId)
        }
    }

    suspend fun getDailyExpenseSync(date: String, userId: Int): Float? {
        return withContext(Dispatchers.IO) {
            recordDao.getDailyExpense(date, userId) ?: 0f
        }
    }
}