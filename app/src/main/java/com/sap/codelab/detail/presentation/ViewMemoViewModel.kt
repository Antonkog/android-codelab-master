package com.sap.codelab.detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for matching ViewMemo view.
 */
internal class ViewMemoViewModel(
    private val repository: IMemoRepository
) : ViewModel() {

    private val _memo: MutableStateFlow<Memo?> = MutableStateFlow(null)
    val memo: StateFlow<Memo?> = _memo

    /**
     * Loads the memo whose id matches the given memoId from the database.
     */
    fun loadMemo(memoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _memo.value = repository.getMemoById(memoId)
        }
    }
}