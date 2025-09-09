package com.sap.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sap.codelab.create.presentation.compose.CreateMemoScreen
import com.sap.codelab.detail.presentation.compose.ViewMemoScreen
import com.sap.codelab.home.presentation.compose.HomeScreen
import com.sap.codelab.ui.theme.AppTheme
import com.sap.codelab.utils.permissions.PermissionsLoadingScreen
import kotlinx.serialization.Serializable

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val nav = rememberNavController()
                AppNavHost(nav)
            }
        }
    }
}

@Composable
fun AppNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = PermissionsLoadingScreen) {
        composable<PermissionsLoadingScreen> {
            PermissionsLoadingScreen(
                onAllPermissionsGranted = {
                    nav.navigate(HomeScreen)
                }
            )
        }
        composable<HomeScreen> {
            HomeScreen(
                onAdd = { nav.navigate(CreateScreen) },
                onOpen = { nav.navigate(ViewScreen) })
        }
        composable<CreateScreen> {
            CreateMemoScreen(
                onBack = { nav.popBackStack() },
                onSave = { nav.popBackStack() })
        }
        composable<ViewScreen> {
            ViewMemoScreen(onBack = { nav.popBackStack() })
        }
    }
}

@Serializable
object PermissionsLoadingScreen

@Serializable
object HomeScreen

@Serializable
object CreateScreen

@Serializable
object ViewScreen