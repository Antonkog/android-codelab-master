package com.sap.codelab.create.presentation

import androidx.lifecycle.ViewModel
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.core.presentation.extensions.empty
import com.sap.codelab.core.utils.coroutines.ScopeProvider
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel(
    private val repository: IMemoRepository
) : ViewModel() {

    private var memo = Memo(0, String.empty(), String.empty(), 0, 0, 0, false)

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        ScopeProvider.application.launch {
            repository.saveMemo(memo)
        }
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = Memo(
            title = title,
            description = description,
            id = 0,
            reminderDate = 0,
            reminderLatitude = 0,
            reminderLongitude = 0,
            isDone = false
        )
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
}