package com.sap.codelab.create.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private val model: CreateMemoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() = with(binding.contentCreateMemo) {
        model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())

        if (model.isMemoValid()) {
            handleValidMemo()
        } else {
            showValidationErrors()
        }
    }

    private fun handleValidMemo() {
        model.saveMemo()
        setResult(RESULT_OK)
        finish()
    }

    private fun showValidationErrors() = with(binding.contentCreateMemo) {
        memoTitleContainer.error = getErrorMessage(
            model.hasTitleError(),
            R.string.memo_title_empty_error
        )
        memoDescription.error = getErrorMessage(
            model.hasTextError(),
            R.string.memo_text_empty_error
        )
    }

    /**
     * Returns the error message if there is an error, or null otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String? =
        if (hasError) getString(errorMessageResId) else null
}
