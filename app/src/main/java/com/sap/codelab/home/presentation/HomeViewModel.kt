package com.sap.codelab.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.home.domain.HomeUseCases
import com.sap.codelab.home.presentation.compose.MemoListAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
class HomeViewModel(
    private val homeUseCases: HomeUseCases
) : ViewModel() {

    /**
     * Internal mutable state flow that holds the current screen state.
     */
    private val _state = MutableStateFlow(MemosListState())

    private var memosJob: Job? = null

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
            .onStart {
                loadMemos(state.value.isShowingAll)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MemosListState()
            )

    /**
     * Switches to observing all/open memos.
     *
     * The database is observed via a Flow, so updates are propagated
     * automatically whenever the underlying data changes.
     */
    fun loadMemos(showAll: Boolean) {
        _state.update { it.copy(isShowingAll = showAll) }
        memosJob?.cancel()
        memosJob = viewModelScope.launch {
            homeUseCases.loadMemos(showAll).collectLatest { memos ->
                _state.update { it.copy(memos = memos) }
            }
        }
    }

    fun onAction(action: MemoListAction) {
        when (action) {
            is MemoListAction.OnMemoChecked -> {
                updateMemo(action.memo, !action.memo.isDone)
            }
        }
    }

    /**
     * Method updates memo as Done, cannot be undone.
     */
    fun updateMemo(memo: Memo, isChecked: Boolean) {
        if (!isChecked) return
        viewModelScope.launch(Dispatchers.IO) {
            homeUseCases.saveMemo(memo.copy(isDone = true))
        }
    }
}
