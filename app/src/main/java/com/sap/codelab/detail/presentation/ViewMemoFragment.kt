package com.sap.codelab.detail.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.databinding.FragmentViewMemoBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewMemoFragment : Fragment() {

    private var _binding: FragmentViewMemoBinding? = null
    private val binding get() = _binding!!
    private val model: ViewMemoViewModel by viewModel()
    private val args: ViewMemoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            model.memo.collect { value ->
                value?.let { memo ->
                    updateUI(memo)
                }
            }
        }
        if (savedInstanceState == null) {
            model.loadMemo(args.memoId)
        }
    }

    private fun updateUI(memo: Memo) {
        binding.run {
            memoTitle.text = memo.title
            memoDescription.text = memo.description
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
