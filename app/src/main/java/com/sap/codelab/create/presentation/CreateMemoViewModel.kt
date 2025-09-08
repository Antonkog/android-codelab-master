package com.sap.codelab.create.presentation

import androidx.lifecycle.ViewModel
import com.sap.codelab.utils.geofence.GeoFenceManager
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel(
    private val repository: IMemoRepository,
    private val geofenceManager: GeoFenceManager
) : ViewModel() {

    private var memo = Memo()

    /**
     * Saves the memo in it's current state.
     */
    suspend fun saveMemo() {
        val newID = repository.saveMemo(memo)
        memo = memo.copy(id = newID)
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = memo.copy(title = title, description = description)
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean =
        memo.title.isNotBlank() && memo.description.isNotBlank() && memo.reminderLatitude != 0.0 && memo.reminderLongitude != 0.0

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()
    fun updateLocation(latitude: Double, longitude: Double) {
        memo = memo.copy(reminderLatitude = latitude, reminderLongitude = longitude)
    }

    fun hasValidLocation(): Boolean {
        return memo.reminderLongitude != 0.0 && memo.reminderLatitude != 0.0
    }

    fun addGeofence() {
        geofenceManager.addGeofence(memo)
    }
}