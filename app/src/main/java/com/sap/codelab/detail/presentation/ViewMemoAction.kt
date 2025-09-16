package com.sap.codelab.detail.presentation


sealed interface ViewMemoAction {
    data class LoadMemo(val memoId: Long) : ViewMemoAction
}