package com.sap.codelab.create.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel(
    private val repository: IMemoRepository
) : ViewModel() {

    private var memo = Memo()

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMemo(memo)
        }
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
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank()

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
       return memo.reminderLongitude !=0.0 && memo.reminderLatitude !=0.0
    }
}