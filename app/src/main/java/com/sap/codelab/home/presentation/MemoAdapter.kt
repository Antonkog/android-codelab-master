package com.sap.codelab.home.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.databinding.ItemRvMemoBinding

/**
 * Callback interface for memo interactions.
 */
interface MemoInteractionListener {
    fun onMemoClicked(memo: Memo)
    fun onCheckboxChanged(memo: Memo, isChecked: Boolean)
}

/**
 * Adapter containing a set of memos.
 */
internal class MemoAdapter(
    private val items: MutableList<Memo>,
    private val listener: MemoInteractionListener
) : RecyclerView.Adapter<MemoViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemRvMemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemoViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        if (position in items.indices) {
            holder.update(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: MemoViewHolder) {
        super.onViewRecycled(holder)
        // Clear listeners to prevent memory leaks
        holder.binding.checkBox.setOnCheckedChangeListener(null)
        holder.binding.root.setOnClickListener(null)
    }

    /**
     * Updates the current list of items to the given list of items using DiffUtil.
     */
    fun setItems(newItems: List<Memo>) {
        val diffCallback = MemoDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }
}


/**
 * ViewHolder for displaying a single memo item.
 */
internal class MemoViewHolder(
    val binding: ItemRvMemoBinding,
    listener: MemoInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            (binding.checkBox.tag as? Memo)?.let { memo ->
                listener.onCheckboxChanged(memo, isChecked)
            }
        }
        binding.root.setOnClickListener {
            (binding.root.tag as? Memo)?.let { memo ->
                listener.onMemoClicked(memo)
            }
        }
    }

    /**
     * Updates the view with the given memo data.
     */
    fun update(memo: Memo) {
        with(binding) {
            memoTitle.text = memo.title.ifEmpty { "Untitled" }
            memoText.text = memo.description.ifEmpty { "No description" }
            checkBox.isChecked = memo.isDone
            checkBox.isEnabled = !memo.isDone
            checkBox.tag = memo
            root.tag = memo
        }
    }
}