package com.sap.codelab.create.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class CreateMemoUiState(
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false
)