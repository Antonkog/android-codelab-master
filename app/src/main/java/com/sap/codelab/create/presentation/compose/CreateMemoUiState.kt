package com.sap.codelab.create.presentation.compose

import androidx.compose.runtime.Immutable

@Immutable
data class CreateMemoUiState(
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null
) {
    val isValid: Boolean
        get() = titleError == null && descriptionError == null && locationError == null
}