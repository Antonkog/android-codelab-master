package com.sap.codelab.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    private val repository: IMemoRepository
) : ViewModel() {

    /**
     * Internal mutable state flow that holds the current state of the brochure list screen.
     */
    private val _state = MutableStateFlow(MemosListState())

    /**
     * Brochure list UI state as a [StateFlow].
     * Keeps upstream active for 5s after the last subscriber to
     * avoid unnecessary reloads during quick lifecycle changes
     * (e.g., orientation change). Starts by calling [loadBrochures].
     */

    val state = _state
        .onStart { loadAllMemos() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            MemosListState()
        )


    /**
     * Loads all memos.
     */
    fun loadAllMemos() {
        _state.update { it.copy(isShowingAll = true) }

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(memos = repository.getAll()) }
        }
    }

    /**
     * Loads all open (not done) memos.
     */
    fun loadOpenMemos() {
        _state.update { it.copy(isShowingAll = false) }

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(memos = repository.getOpen()) }
        }
    }

    fun refreshMemos() {
        if (state.value.isShowingAll) {
            loadAllMemos()
        } else {
            loadOpenMemos()
        }
    }

    fun updateMemo(memo: Memo, isChecked: Boolean) {
        if (!isChecked) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMemo(memo.copy(isDone = true))
        }
    }
}
