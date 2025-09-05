package com.sap.codelab.home.presentation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sap.codelab.R
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.create.presentation.CreateMemo
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.detail.presentation.BUNDLE_MEMO_ID
import com.sap.codelab.detail.presentation.ViewMemo
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sap.codelab.utils.permissions.PermissionsHandler

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 */
internal class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModel()
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem
    private val createMemoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.refreshMemos()
            }
        }

    private lateinit var permissionsHandler: PermissionsHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Setup the adapter and the recycler view
        setupRecyclerView(initializeAdapter())

        // Setup permissions handler and request permissions with rationale on first open
        permissionsHandler = PermissionsHandler(
            activity = this,
            caller = this
        )
            .apply {
            callback = object : PermissionsHandler.Callback {
                override fun onNotificationPermissionGranted() {
                    binding.fab.visibility = View.VISIBLE
                }

                override fun onNotificationPermissionDenied() {
                    binding.fab.visibility = View.GONE
                }

                override fun onAllLocationPermissionsGranted() {
                    binding.fab.visibility = View.VISIBLE
                }

                override fun onLocationPermissionDenied(permission: String) {
                    binding.fab.visibility = View.GONE
                }

            }
        }
        permissionsHandler.ensureLocationPermissions()
        binding.fab.setOnClickListener {
            // Handles clicks on the FAB button > creates a new Memo
            createMemoLauncher.launch(Intent(this@Home, CreateMemo::class.java))
        }
        viewModel.loadOpenMemos()
    }

    /**
     * Initializes the adapter and sets the needed callbacks.
     */
    private fun initializeAdapter(): MemoAdapter {
        val adapter = MemoAdapter(mutableListOf(), { view ->
            // Implementation for when the user selects a row to show the detail view
            showMemo((view.tag as Memo).id)
        }, { checkbox, isChecked ->
            // Implementation for when the user marks a memo as completed
            viewModel.updateMemo(checkbox.tag as Memo, isChecked)
            viewModel.refreshMemos()
        })

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.setItems(state.memos)
            }
        }
        return adapter
    }

    /**
     * Opens the Memo detail view for the given memoId.
     *
     * @param memoId    - the id of the memo to be shown.
     */
    private fun showMemo(memoId: Long) {
        val intent = Intent(this@Home, ViewMemo::class.java)
        intent.putExtra(BUNDLE_MEMO_ID, memoId)
        startActivity(intent)
    }

    /**
     * Initializes the recycler view to display the list of memos.
     */
    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@Home,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                viewModel.loadAllMemos()
                //Switch available menu options
                menuItemShowAll.isVisible = false
                menuItemShowOpen.isVisible = true
                true
            }

            R.id.action_show_open -> {
                viewModel.loadOpenMemos()
                //Switch available menu options
                menuItemShowOpen.isVisible = false
                menuItemShowAll.isVisible = true
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
