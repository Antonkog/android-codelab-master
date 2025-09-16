package com.sap.codelab.home.presentation

import com.sap.codelab.core.domain.Memo

sealed interface MemoListAction {
    data class OnMemoChecked(val memo: Memo) : MemoListAction
    data class LoadMemos(val showAll: Boolean) : MemoListAction
}