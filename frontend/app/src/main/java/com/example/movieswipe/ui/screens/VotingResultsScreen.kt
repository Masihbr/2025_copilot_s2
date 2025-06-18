package com.example.movieswipe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movieswipe.network.Movie
import com.example.movieswipe.network.api
import kotlinx.coroutines.launch

@Composable
fun VotingResultsScreen(
    navController: NavController,
    sessionId: String,
    onBackToGroup: () -> Unit
) {
    var matchedMovie by remember { mutableStateOf<Movie?>(null) }
    var votes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<String?>(null) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId) {
        loading = true
        error = null
        try {
            val resp = api.getVotingResults(sessionId)
            if (resp.isSuccessful) {
                val result = resp.body()
                matchedMovie = result?.matchedMovie
                votes = result?.votes ?: emptyMap()
                // Optionally fetch session details or movies if needed
            } else {
                error = "Failed to load voting results."
            }
        } catch (e: Exception) {
            error = e.localizedMessage
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Voting Results", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            matchedMovie?.let { movie ->
                Text("Matched Movie:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(movie.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(movie.overview ?: "No description available.")
                    }
                }
            } ?: Text("No matched movie found.")
            Spacer(Modifier.height(24.dp))
            Text("Votes:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(votes.entries.toList()) { (user, vote) ->
                    Text("$user: $vote")
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBackToGroup) { Text("Back to Group") }
        }
    }
}
