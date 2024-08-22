package com.zeusinstitute.upiapp

// ... other imports ...
import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class UPIAPP : Application() {
    val database: AppDatabase by lazy {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE PayTransaction ADD COLUMN name TEXT NOT NULL")
            }
        }

        Room.databaseBuilder(this, AppDatabase::class.java, "transactions")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }
}