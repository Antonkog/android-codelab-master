package com.sap.codelab.core.domain

import com.sap.codelab.core.presentation.extensions.empty

/**
 * Domain model representing a memo (business layer).
 */
data class Memo(
    val id: Long = 0,
    val title: String = String.empty(),
    val description: String = String.empty(),
    val reminderDate: Long = 0,
    val reminderLatitude: Long = 0,
    val reminderLongitude: Long = 0,
    val isDone: Boolean = false
)