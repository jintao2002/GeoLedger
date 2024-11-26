package com.cos407.cs407finalproject.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records WHERE userId = :userId")
    suspend fun getAllRecordsByUser(userId: Int): List<Record>

    @Delete
    suspend fun delete(record: Record)

    @Update
    suspend fun update(record: Record)

    @Query("SELECT SUM(amount) FROM records WHERE date = :date AND userId = :userId")
    suspend fun getDailyExpense(date: String, userId: Int): Float?

}