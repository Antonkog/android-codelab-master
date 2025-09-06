package com.sap.codelab.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * That database that is used to store information.
 */
@Database(entities = [MemoEntity::class], version = 2, exportSchema = true)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME: String = "codelab"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}