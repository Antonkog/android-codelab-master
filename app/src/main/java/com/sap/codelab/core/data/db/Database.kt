package com.sap.codelab.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sap.codelab.core.domain.Memo

/**
 * That database that is used to store information.
 */
@Database(entities = [Memo::class], version = 1, exportSchema = false)
internal abstract class Database : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao
}