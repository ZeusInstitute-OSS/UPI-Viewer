package com.zeusinstitute.upiapp

import android.app.Application
import androidx.room.Room

class UPIAPP : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()
    }
}