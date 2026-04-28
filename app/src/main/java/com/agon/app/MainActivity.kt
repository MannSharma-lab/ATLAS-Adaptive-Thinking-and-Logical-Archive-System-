package com.agon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import com.agon.app.ui.screens.CategoryScreen
import com.agon.app.ui.screens.HomeScreen
import com.agon.app.ui.screens.PreviewScreen
import com.agon.app.ui.screens.SettingsScreen
import com.agon.app.ui.screens.SplashScreen
import com.agon.app.ui.theme.AgonAppTheme
import com.agon.app.viewmodel.AtlasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: AtlasViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isSystemDefaultTheme by viewModel.isSystemDefaultTheme.collectAsState()
            
            val darkTheme = if (isSystemDefaultTheme) {
                androidx.compose.foundation.isSystemInDarkTheme()
            } else {
                isDarkMode
            }
            
            AgonAppTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AtlasApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun AtlasApp(viewModel: AtlasViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCategory = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable(
            route = "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryScreen(
                categoryId = categoryId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPreview = { screenshotId ->
                    navController.navigate("preview/$screenshotId")
                }
            )
        }
        composable(
            route = "preview/{screenshotId}",
            arguments = listOf(navArgument("screenshotId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Use URI decoding in case the string was encoded when navigating
            val screenshotId = Uri.decode(backStackEntry.arguments?.getString("screenshotId") ?: "")
            PreviewScreen(
                screenshotId = screenshotId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
