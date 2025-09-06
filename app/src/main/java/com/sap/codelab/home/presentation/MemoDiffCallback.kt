package com.sap.codelab.home.presentation

import androidx.recyclerview.widget.DiffUtil
import com.sap.codelab.core.domain.Memo

class MemoDiffCallback(
    private val oldList: List<Memo>,
    private val newList: List<Memo>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}