package com.sap.codelab.create.presentation.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.utils.geofence.GeoFenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateMemoNewViewModel(
    private val repository: IMemoRepository,
    private val geoFenceManager: GeoFenceManager
) : ViewModel() {

    private var memo = Memo()

    private val _uiState = MutableStateFlow(CreateMemoUiState())
    val uiState: StateFlow<CreateMemoUiState> = _uiState

    fun onAction(action: CreateMemoAction) {
        when (action) {
            is CreateMemoAction.OnTitleChange -> {
                memo = memo.copy(title = action.title)
                _uiState.update { it.copy(title = action.title, titleError = false) }
            }

            is CreateMemoAction.OnDescriptionChange -> {
                memo = memo.copy(description = action.description)
                _uiState.update {
                    it.copy(
                        description = action.description,
                        descriptionError = false
                    )
                }
            }

            is CreateMemoAction.OnLocationSelected -> {
                memo = memo.copy(
                    reminderLatitude = action.latitude,
                    reminderLongitude = action.longitude
                )
                _uiState.update {
                    it.copy(
                        latitude = action.latitude,
                        longitude = action.longitude,
                        locationError = false
                    )
                }
            }

            is CreateMemoAction.OnSave -> {
                saveMemo()
            }
        }
    }

    fun validate(): Boolean {
        val current = _uiState.value
        var titleError = false
        var descriptionError = false
        var locationError = false

        if (current.title.isBlank()) {
            titleError = true
        }
        if (current.description.isBlank()) {
            descriptionError = true
        }
        if (current.latitude == 0.0 || current.longitude == 0.0) {
            locationError = true
        }

        _uiState.value = current.copy(
            titleError = titleError,
            descriptionError = descriptionError,
            locationError = locationError
        )
        return !titleError && !descriptionError && !locationError
    }

    private fun saveMemo() = viewModelScope.launch {
        val newId = repository.saveMemo(memo)
        memo = memo.copy(id = newId)
        geoFenceManager.addGeofence(memo)
    }
}
