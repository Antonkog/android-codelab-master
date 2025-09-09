package com.sap.codelab.home.presentation.compose

import com.sap.codelab.core.domain.Memo

sealed interface MemoListAction {
    data class OnMemoChecked(val memo: Memo): MemoListAction
}