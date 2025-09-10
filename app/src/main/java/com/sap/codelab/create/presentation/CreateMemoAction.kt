package com.sap.codelab.create.presentation

sealed interface CreateMemoAction {
    data class OnTitleChange(val title: String) : CreateMemoAction
    data class OnDescriptionChange(val description: String) : CreateMemoAction
    data class OnLocationSelected(val latitude: Double, val longitude: Double) : CreateMemoAction
    object OnSave : CreateMemoAction
}