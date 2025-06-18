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
import com.example.movieswipe.network.Group
import com.example.movieswipe.network.Movie
import com.example.movieswipe.network.api
import kotlinx.coroutines.launch

@Composable
fun GroupDetailsScreen(navController: NavController, groupId: String) {
    var group by remember { mutableStateOf<Group?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var votingLoading by remember { mutableStateOf(false) }
    var votingError by remember { mutableStateOf<String?>(null) }
    var votingSessionId by remember { mutableStateOf<String?>(null) }
    var votingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var pastSessions by remember { mutableStateOf<List<String>>(emptyList()) } // session IDs
    var pastSessionsLoading by remember { mutableStateOf(false) }
    var pastSessionsError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        loading = true
        error = null
        pastSessionsLoading = true
        pastSessionsError = null
        try {
            val response = api.getGroup(groupId)
            if (response.isSuccessful) {
                group = response.body()
                // TODO: Fetch past sessions for this group if backend supports it
                // For now, leave as empty
                pastSessions = emptyList()
            } else {
                error = "Failed to load group."
            }
        } catch (e: Exception) {
            error = e.localizedMessage
        } finally {
            loading = false
            pastSessionsLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            group?.let { g ->
                Text(g.name, style = MaterialTheme.typography.headlineMedium)
                Text("Invite Code: ${g.inviteCode}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text("Members:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(g.members) { member ->
                        Text("- ${member.name ?: member.email}")
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        // Start voting session: fetch recommended genres, discover movies, and start session
                        scope.launch {
                            votingLoading = true
                            votingError = null
                            try {
                                val genresResp = api.getRecommendedGenres(g.id)
                                val genres = genresResp.body()?.map { it.id }?.joinToString(",") ?: ""
                                val moviesResp = api.discoverMovies(genres, 1)
                                val movies = moviesResp.body() ?: emptyList()
                                val movieIds = movies.map { it.id }
                                val startResp = api.startVoting(mapOf("groupId" to g.id, "movies" to movieIds))
                                if (startResp.isSuccessful) {
                                    val session = startResp.body()
                                    votingSessionId = session?.sessionId
                                    votingMovies = movies
                                    navController.navigate("votingSession/${g.id}/${session?.sessionId}")
                                } else {
                                    votingError = "Failed to start voting session."
                                }
                            } catch (e: Exception) {
                                votingError = e.localizedMessage
                            } finally {
                                votingLoading = false
                            }
                        }
                    }) {
                        Text("Start Voting")
                    }
                    Button(onClick = {
                        // End voting session
                        scope.launch {
                            votingLoading = true
                            votingError = null
                            try {
                                val resp = api.endVoting(mapOf("groupId" to g.id))
                                if (resp.isSuccessful) {
                                    // Assume latest sessionId is available
                                    navController.navigate("votingResults/${votingSessionId ?: ""}")
                                } else {
                                    votingError = "Failed to end voting session."
                                }
                            } catch (e: Exception) {
                                votingError = e.localizedMessage
                            } finally {
                                votingLoading = false
                            }
                        }
                    }) {
                        Text("End Voting")
                    }
                    Button(onClick = {
                        scope.launch {
                            try {
                                val resp = api.deleteGroup(g.id)
                                if (resp.isSuccessful) {
                                    navController.popBackStack()
                                } else {
                                    error = "Failed to delete group."
                                }
                            } catch (e: Exception) {
                                error = e.localizedMessage
                            }
                        }
                    }) {
                        Text("Delete Group")
                    }
                }
                votingError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (votingLoading) CircularProgressIndicator()

                Spacer(Modifier.height(24.dp))
                Text("Past Voting Sessions:", style = MaterialTheme.typography.titleMedium)
                if (pastSessionsLoading) {
                    CircularProgressIndicator()
                } else if (pastSessionsError != null) {
                    Text("Error: $pastSessionsError", color = MaterialTheme.colorScheme.error)
                } else if (pastSessions.isEmpty()) {
                    Text("No past sessions.")
                } else {
                    LazyColumn {
                        items(pastSessions) { sessionId ->
                            Button(onClick = { navController.navigate("votingResults/$sessionId") }) {
                                Text("Session $sessionId")
                            }
                        }
                    }
                }
            }
        }
    }
}
