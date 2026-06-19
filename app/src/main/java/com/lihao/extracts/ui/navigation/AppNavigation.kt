package com.lihao.extracts.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lihao.extracts.ui.edit.EditScreen
import com.lihao.extracts.ui.home.HomeScreen
import com.lihao.extracts.ui.category.CategoryScreen
import com.lihao.extracts.ui.settings.SettingsScreen
import com.lihao.extracts.viewmodel.EditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onAddNote = { navController.navigate("edit") },
                onEditNote = { noteId -> navController.navigate("edit/$noteId") },
                onSettingsClick = { navController.navigate("settings") },
                onCategoryClick = { navController.navigate("category") }
            )
        }
        composable("edit") {
            val viewModel: EditViewModel = viewModel()
            EditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("edit/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
            val viewModel: EditViewModel = viewModel()
            noteId?.let { viewModel.loadNote(it) }
            EditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("category") {
            CategoryScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
