package com.example.farm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.farm.ui.screen.AnalysisScreen
import com.example.farm.ui.screen.HomeScreen
import androidx.navigation.NavBackStackEntry
import androidx.compose.runtime.remember

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { backStackEntry ->
            HomeScreen(
                onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                navController = navController,
                parentEntry = backStackEntry
            )
        }
        
        composable(Screen.Analysis.route) { backStackEntry ->
            // Get the parent entry for the Home route to share the ViewModel
            val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.Home.route) }
            AnalysisScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController,
                parentEntry = parentEntry
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Analysis : Screen("analysis")
} 