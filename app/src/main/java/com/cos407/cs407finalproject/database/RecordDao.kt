package com.cos407.cs407finalproject.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records WHERE userId = :userId")
    suspend fun getAllRecordsByUser(userId: Int): List<Record>

    data class DailyExpenseSummary(
        val day: String,
        val total: Double
    )

    data class CategoryExpenseSummary(
        val category: String,
        val total: Double
    )

    // summarize by categories
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM records 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY category
    """)
    suspend fun getMonthlyExpensesByCategory(
        userId: Int,
        startDate: Long,
        endDate: Long
    ): List<CategoryExpenseSummary>


    // get daily total
    @Query("""
        SELECT strftime('%Y-%m-%d', date/1000, 'unixepoch') as day, 
        SUM(amount) as total 
        FROM records 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY day
    """)
    suspend fun getDailyExpense(
        userId: Int,
        startDate: Long,
        endDate: Long
    ): List<DailyExpenseSummary>


    // get monthly total
    @Query("""
        SELECT SUM(amount) 
        FROM records 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getMonthlyTotal(userId: Int, startDate: Long, endDate: Long): Double?


    @Delete
    suspend fun delete(record: Record)

    @Update
    suspend fun update(record: Record)

//    @Query("SELECT SUM(amount) FROM records WHERE date = :date AND userId = :userId")
//    suspend fun getDailyExpense(date: String, userId: Int): Float?

}