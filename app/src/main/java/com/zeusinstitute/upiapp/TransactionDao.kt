package com.zeusinstitute.upiapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    fun insert(transaction: PayTransaction)

    @Query("SELECT * FROM PayTransaction ORDER BY date DESC")
    fun getAll(): Flow<List<PayTransaction>>

    @Query("DELETE FROM PayTransaction")
    fun deleteAll()

    @Query("SELECT * FROM PayTransaction")
    fun getAllTransactions(): List<PayTransaction>
}