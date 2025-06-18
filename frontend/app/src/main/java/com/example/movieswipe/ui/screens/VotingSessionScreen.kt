package com.example.movieswipe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movieswipe.network.Movie
import com.example.movieswipe.network.api
import kotlinx.coroutines.launch

@Composable
fun VotingSessionScreen(
    navController: NavController,
    groupId: String,
    sessionId: String?,
    movies: List<Movie>?,
    onVoteComplete: (String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var voting by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var sessionMovies by remember { mutableStateOf<List<Movie>>(movies ?: emptyList()) }
    val scope = rememberCoroutineScope()

    // Fetch movies if not provided
    LaunchedEffect(sessionId) {
        if (sessionMovies.isEmpty() && sessionId != null) {
            // Optionally fetch session details to get movies
            // Not implemented: depends on backend
        }
    }

    fun voteForMovie(movieId: String, vote: String) {
        scope.launch {
            try {
                val resp = api.vote(mapOf("groupId" to groupId, "movieId" to movieId, "vote" to vote))
                if (resp.isSuccessful) {
                    if (currentIndex < sessionMovies.size - 1) {
                        currentIndex++
                    } else {
                        voting = false
                        onVoteComplete(sessionId ?: "")
                    }
                } else {
                    error = "Failed to record vote."
                }
            } catch (e: Exception) {
                error = e.localizedMessage
            }
        }
    }

    if (!voting) {
        Text("Thank you for voting! Waiting for results...", modifier = Modifier.padding(32.dp))
        return
    }

    val movie = sessionMovies.getOrNull(currentIndex)
    if (movie == null) {
        Text("No movies to vote on.", modifier = Modifier.padding(32.dp))
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 100) {
                        voteForMovie(movie.id, "yes")
                    } else if (dragAmount < -100) {
                        voteForMovie(movie.id, "no")
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.LightGray),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(movie.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(movie.overview ?: "No description available.")
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { voteForMovie(movie.id, "no") }) { Text("No") }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = { voteForMovie(movie.id, "yes") }) { Text("Yes") }
                }
            }
        }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
}
