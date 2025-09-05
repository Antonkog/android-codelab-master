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

/**
 * HomeViewModel responsible for exposing the current memos list UI state.
 *
 * It observes database changes through repository flows and keeps the state
 * updated automatically.
 */
internal class HomeViewModel(
    private val repository: IMemoRepository
) : ViewModel() {
    
    /**
     * Internal mutable state flow that holds the current screen state.
     */
    private val _state = MutableStateFlow(MemosListState())

    /**
     * Publicly exposed UI state as a [StateFlow].
     *
     * - Keeps the upstream flow active for 5 seconds after the last collector
     *   (avoids unnecessary reloads during quick lifecycle changes such as
     *   configuration changes).
     * - Starts by loading all memos.
     */
    val state: StateFlow<MemosListState> =
        _state
            .onStart { loadAllMemos() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MemosListState()
            )

    /**
     * Switches to observing all memos.
     *
     * The database is observed via a [Flow], so updates are propagated
     * automatically whenever the underlying data changes.
     */
    fun loadAllMemos() {
        _state.update { it.copy(isShowingAll = true) }

        viewModelScope.launch {
            repository.getAllMemoAsFlow().collect { memos ->
                _state.update { it.copy(memos = memos) }
            }
        }
    }

    /**
     * Switches to observing only open memos (not marked as done).
     *
     * The database is observed via a [Flow], so updates are propagated
     * automatically whenever the underlying data changes.
     */
    fun loadOpenMemos() {
        _state.update { it.copy(isShowingAll = false) }

        viewModelScope.launch {
            repository.getOpenMemoAsFlow().collect { memos ->
                _state.update { it.copy(memos = memos) }
            }
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
