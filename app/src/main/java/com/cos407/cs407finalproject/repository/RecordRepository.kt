package com.cos407.cs407finalproject.repository

import android.util.Log
import java.util.Calendar
import com.cos407.cs407finalproject.database.Record
import com.cos407.cs407finalproject.database.RecordDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class MonthlyStatistics(
    val categoryExpenses: Map<String, Double>,
    val dailyExpenses: Map<String, Double>,
    val totalExpense: Double
)

class RecordRepository(private val recordDao: RecordDao) {

    private val firebaseDb = FirebaseFirestore.getInstance()

    suspend fun getDailyExpenseSync(
        userId: Int,
        startDate: Long,
        endDate: Long
    ): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            recordDao.getDailyExpense(userId, startDate, endDate)
                .associate { it.day to it.total }
        }
    }

    // Modify point: insert only locally, don't wait for Firebase operation
    suspend fun saveRecord(record: Record): Long {
        return withContext(Dispatchers.IO) {
            val id = recordDao.insert(record)
            id
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

    suspend fun getMonthlyStatistics(userId: Int, year: Int, month: Int): MonthlyStatistics {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            val startDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.timeInMillis - 1

            val dailyExpenses = recordDao.getDailyExpense(userId, startDate, endDate)
                .associate { it.day to it.total }

            val categoryExpenses = recordDao.getMonthlyExpensesByCategory(userId, startDate, endDate)
                .associate { it.category to it.total }

            MonthlyStatistics(
                categoryExpenses = categoryExpenses,
                dailyExpenses = dailyExpenses,
                totalExpense = recordDao.getMonthlyTotal(userId, startDate, endDate) ?: 0.0
            )
        }
    }

    suspend fun getStatisticsForDateRange(
        userId: Int,
        startDate: Long,
        endDate: Long
    ): MonthlyStatistics {
        return withContext(Dispatchers.IO) {
            val dailyExpenses = recordDao.getDailyExpense(userId, startDate, endDate)
                .associate { it.day to it.total }

            val categoryExpenses = recordDao.getMonthlyExpensesByCategory(userId, startDate, endDate)
                .associate { it.category to it.total }

            MonthlyStatistics(
                categoryExpenses = categoryExpenses,
                dailyExpenses = dailyExpenses,
                totalExpense = recordDao.getMonthlyTotal(userId, startDate, endDate) ?: 0.0
            )
        }
    }
}
