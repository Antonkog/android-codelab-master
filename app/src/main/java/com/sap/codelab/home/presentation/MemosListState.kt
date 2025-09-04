package com.sap.codelab.home.presentation

import androidx.compose.runtime.Immutable
import com.sap.codelab.core.domain.Memo

@Immutable
data class MemosListState(
    val isLoading: Boolean = false,
    val isShowingAll: Boolean = false,
    val memos: List<Memo> = emptyList()
)
