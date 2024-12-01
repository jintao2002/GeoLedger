package com.cos407.cs407finalproject.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double = 0.0,
    val item: String = "",
    val category: String = "Uncategorized",
    val location: String = "",
    val date: Long = 0L,
    val userId: Int = 0
)   {
    constructor() : this(0, 0.0, "", "Uncategorized", "", 0L, 0)
}

object ExpenseCategories {
    const val FOOD = "Food"
    const val TRANSPORTATION = "Transportation"
    const val SHOPPING = "Shopping"
    const val GROCERY = "Grocery"
    const val POWER = "Power"
    const val EDUCATION = "Education"
    const val SNACK = "Snack"
    const val CLOTHS = "Cloths"
    const val FURNITURE = "Furniture"
    const val FITNESS = "Fitness"
    const val COMMUNICATION = "Communication"
    const val TRAVEL = "Travel"
    const val GIFT = "Gift"
    const val GAMES = "Games"
    const val PETS = "Pets"

    // more categories...
}