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
import com.example.movieswipe.network.Genre
import com.example.movieswipe.network.api
import kotlinx.coroutines.launch

@Composable
fun GenrePreferencesScreen(navController: NavController, onPreferencesSet: () -> Unit) {
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }
    var selectedGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val response = api.getGenres()
            if (response.isSuccessful) {
                genres = response.body() ?: emptyList()
            } else {
                error = "Failed to load genres."
            }
        } catch (e: Exception) {
            error = e.localizedMessage
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Your Favorite Genres", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(genres) { genre ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedGenres.contains(genre.id),
                            onCheckedChange = { checked ->
                                selectedGenres = if (checked) selectedGenres + genre.id else selectedGenres - genre.id
                            }
                        )
                        Text(genre.name)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val resp = api.setPreferences(mapOf("preferences" to selectedGenres.toList()))
                            if (resp.isSuccessful) {
                                onPreferencesSet()
                            } else {
                                error = "Failed to save preferences."
                            }
                        } catch (e: Exception) {
                            error = e.localizedMessage
                        }
                    }
                },
                enabled = selectedGenres.isNotEmpty()
            ) {
                Text("Save Preferences")
            }
        }
    }
}

