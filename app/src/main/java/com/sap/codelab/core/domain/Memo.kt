package com.sap.codelab.core.domain

/**
 * Domain model representing a memo (business layer).
 */
data class Memo(
    val id: Long,
    val title: String,
    val description: String,
    val reminderDate: Long,
    val reminderLatitude: Long,
    val reminderLongitude: Long,
    val isDone: Boolean = false
)