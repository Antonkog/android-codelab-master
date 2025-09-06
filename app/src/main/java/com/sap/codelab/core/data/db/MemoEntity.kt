package com.sap.codelab.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a memo in the database.
 */
@Entity(tableName = "memo")
data class MemoEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "reminderDate") val reminderDate: Long,
    @ColumnInfo(name = "reminderLatitude") val reminderLatitude: Double,
    @ColumnInfo(name = "reminderLongitude") val reminderLongitude: Double,
    @ColumnInfo(name = "isDone") val isDone: Boolean = false,
    @ColumnInfo(name = "notificationShown") val notificationShown: Boolean = false
)