package com.zeusinstitute.upiapp

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}