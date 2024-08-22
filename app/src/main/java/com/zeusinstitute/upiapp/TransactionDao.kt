package com.zeusinstitute.upiapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: com.zeusinstitute.upiapp.Transaction)

    @Query("SELECT * FROM Transaction ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("DELETE FROM Transaction")
    suspend fun deleteAll()

    @Query("SELECT * FROM Transaction")
    suspend fun getAllTransactions(): List<Transaction>
}