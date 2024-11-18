package com.cos407.cs407finalproject.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: String,
    val item: String,
    val location: String,
    val date: String,
    val userId: Int
)
