package com.sap.codelab.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.sap.codelab.R
import com.sap.codelab.home.presentation.HomeFragmentDirections
import com.sap.codelab.utils.Constants.BUNDLE_MEMO_ID

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        handleIntentForNavigation(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntentForNavigation(intent)
    }

    private fun handleIntentForNavigation(intent: android.content.Intent) {
        val memoId = intent.getLongExtra(BUNDLE_MEMO_ID, -1L)
        if (memoId != -1L) {
            val action = HomeFragmentDirections.actionHomeFragmentToViewMemoFragment(
                memoId
            )
            navController.navigate(action)
        }
    }
}
