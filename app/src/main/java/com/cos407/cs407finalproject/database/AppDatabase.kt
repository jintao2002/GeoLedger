package com.cos407.cs407finalproject.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Record::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {


    // Data Access Objects (DAOs) for the entities
    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Create or get an instance of the database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "geoledger_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Dynamically switch database or reinitialize for a new user context
        fun switchDatabase(context: Context, userId: Int): AppDatabase {
            synchronized(this) {
                // Optional: Add a user-specific prefix or suffix if needed for multi-user environments
                val dbName = "geoledger_database_user_$userId"
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, dbName
                ).fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }


    }
}