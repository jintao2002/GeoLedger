package com.cos407.cs407finalproject.repository

import android.util.Log
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.database.RecordDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecordRepository(private val recordDao: RecordDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()

    // Save a record to local database and Firebase
    suspend fun saveRecord(record: Record) {
        // Switch to IO dispatcher for database operations
        withContext(Dispatchers.IO) {
            // Save to local database
            recordDao.insert(record)

            // Save to Firebase
            firebaseDb.collection("records").add(record).addOnSuccessListener {
                Log.d("RecordRepository", "Record saved to Firebase")
            }.addOnFailureListener {
                Log.e("RecordRepository", "Failed to save record to Firebase", it)
            }
        }
    }

    // Synchronize records from Firebase to Room
    suspend fun syncRecords(userId: Int) {
        withContext(Dispatchers.IO) {
            try {
                // Convert Firebase Task to a coroutine
                val documents =
                    firebaseDb.collection("records").whereEqualTo("userId", userId).get().await()

                for (document in documents) {
                    val record = document.toObject(Record::class.java)
                    recordDao.insert(record) // Save to local database
                }
            } catch (e: Exception) {
                Log.e("RecordRepository", "Failed to fetch records from Firebase", e)
            }
        }
    }

    // Delete data
    suspend fun deleteRecord(record: Record) {
        withContext(Dispatchers.IO) {
            // Delete from local database
            recordDao.delete(record)

            // Delete from Firebase
            try {
                // Assuming you store the Firebase document ID in the record or can query it
                val querySnapshot =
                    firebaseDb.collection("records").whereEqualTo("id", record.id).get().await()
                for (document in querySnapshot.documents) {
                    firebaseDb.collection("records").document(document.id).delete().await()
                }
                Log.d("RecordRepository", "Record deleted from Firebase")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Failed to delete record from Firebase", e)
            }
        }
    }

    // Update the data
    suspend fun updateRecord(record: Record) {
        withContext(Dispatchers.IO) {
            // Update in local database
            recordDao.update(record)

            // Update in Firebase
            try {
                val querySnapshot =
                    firebaseDb.collection("records").whereEqualTo("id", record.id).get().await()
                for (document in querySnapshot.documents) {
                    firebaseDb.collection("records").document(document.id).set(record).await()
                }
                Log.d("RecordRepository", "Record updated in Firebase")
            } catch (e: Exception) {
                Log.e("RecordRepository", "Failed to update record in Firebase", e)
            }
        }
    }

    // Retrieve all records for a user from local Room database
    suspend fun getRecordsByUser(userId: Int): List<Record> {
        return withContext(Dispatchers.IO) {
            recordDao.getAllRecordsByUser(userId)
        }
    }
}
