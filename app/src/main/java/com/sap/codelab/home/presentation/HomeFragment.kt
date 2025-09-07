package com.sap.codelab.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sap.codelab.R
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.databinding.FragmentHomeBinding
import com.sap.codelab.utils.permissions.PermissionsHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModel()
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem

    private lateinit var permissionsHandler: PermissionsHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        // Setup the adapter and the recycler view
        setupRecyclerView(initializeAdapter())

        // Permissions handler requires an Activity and caller Fragment
        permissionsHandler = PermissionsHandler(requireActivity(), this).apply {
            callback = object : PermissionsHandler.Callback {
                override fun onNotificationPermissionGranted() {
                    binding.fab.visibility = View.VISIBLE
                    startLocationMonitoringService()
                }

                override fun onNotificationPermissionDenied() {
                    binding.fab.visibility = View.GONE
                }

                override fun onAllLocationPermissionsGranted() {
                    binding.fab.visibility = View.VISIBLE
                    startLocationMonitoringService()
                }

                override fun onLocationPermissionDenied(permission: String) {
                    binding.fab.visibility = View.GONE
                }
            }
        }
        permissionsHandler.ensureLocationPermissions()

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createMemoFragment)
        }
        viewModel.loadOpenMemos()
    }

    private fun initializeAdapter(): MemoAdapter {
        val adapter = MemoAdapter(mutableListOf(), object : MemoInteractionListener {
            override fun onMemoClicked(memo: Memo) {
                val action = HomeFragmentDirections.actionHomeFragmentToViewMemoFragment(memo.id)
                findNavController().navigate(action)
            }

            override fun onCheckboxChanged(memo: Memo, isChecked: Boolean) {
                viewModel.updateMemo(memo, isChecked)
                viewModel.refreshMemos()
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.setItems(state.memos)
            }
        }
        return adapter
    }

    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.recyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    private fun setupMenu() {
        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
                menuItemShowAll = menu.findItem(R.id.action_show_all)
                menuItemShowOpen = menu.findItem(R.id.action_show_open)
                // initialize visibility according to state
                val showingAll = viewModel.state.value.isShowingAll
                menuItemShowAll.isVisible = !showingAll
                menuItemShowOpen.isVisible = showingAll
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_show_all -> {
                        viewModel.loadAllMemos()
                        menuItemShowAll.isVisible = false
                        menuItemShowOpen.isVisible = true
                        true
                    }
                    R.id.action_show_open -> {
                        viewModel.loadOpenMemos()
                        menuItemShowOpen.isVisible = false
                        menuItemShowAll.isVisible = true
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun startLocationMonitoringService() {
        if (!com.sap.codelab.core.presentation.LocationService.isRunning()) {
            val intent = android.content.Intent(
                requireContext(),
                com.sap.codelab.core.presentation.LocationService::class.java
            )
            // Start as a normal service while app is in foreground; it will be promoted to foreground when app goes to background.
            requireContext().startService(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
