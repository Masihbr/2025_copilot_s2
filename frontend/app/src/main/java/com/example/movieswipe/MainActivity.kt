package com.example.movieswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movieswipe.ui.screens.GoogleSignInScreen
import com.example.movieswipe.ui.screens.HomeScreen
import com.example.movieswipe.ui.screens.GroupDetailsScreen
import com.example.movieswipe.ui.screens.GenrePreferencesScreen
import com.example.movieswipe.ui.screens.VotingSessionScreen
import com.example.movieswipe.ui.screens.VotingResultsScreen
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.navigation.NavType
import androidx.navigation.compose.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSwipeTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MovieSwipeApp()
                }
            }
        }
    }
}

@Composable
fun MovieSwipeApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "sign_in") {
        composable("sign_in") { GoogleSignInScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable(
            "groupDetails/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailsScreen(navController, groupId)
        }
        composable("genrePreferences") {
            GenrePreferencesScreen(navController) {
                navController.navigate("home") { popUpTo("genrePreferences") { inclusive = true } }
            }
        }
        composable(
            "votingSession/{groupId}/{sessionId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            VotingSessionScreen(
                navController = navController,
                groupId = groupId,
                sessionId = sessionId,
                movies = null,
                onVoteComplete = { sid ->
                    navController.navigate("votingResults/$sid")
                }
            )
        }
        composable(
            "votingResults/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            VotingResultsScreen(navController, sessionId) {
                navController.navigate("home") { popUpTo("votingResults/{sessionId}") { inclusive = true } }
            }
        }
    }
}
