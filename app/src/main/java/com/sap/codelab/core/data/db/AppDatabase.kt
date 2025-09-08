package com.sap.codelab.core.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

/**
 * That database that is used to store information.
 */
@Database(
    entities = [MemoEntity::class], version = 3, exportSchema = true,
    autoMigrations = [
        AutoMigration(2, 3, spec = AppDatabase.AutoMigration2to3::class)
    ]
)
internal abstract class AppDatabase : RoomDatabase() {

    @RenameColumn(
        tableName = "memo",
        fromColumnName = "notificationShown",
        toColumnName = "isNotificationShown"
    )
    class AutoMigration2to3 : AutoMigrationSpec

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