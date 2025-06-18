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
import com.example.movieswipe.network.MovieSwipeApi
import com.example.movieswipe.network.api
import com.example.movieswipe.network.Group
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            // Fetch all groups for the user (simulate by fetching all groups if needed)
            // If backend does not provide a list, you may need to fetch from /group/{groupId} for each groupId
            // For now, let's fetch the user's info and use a mock list for demonstration
            val userResponse = api.getCurrentUser()
            if (userResponse.isSuccessful) {
                // TODO: Replace with actual group fetching logic if backend provides group IDs
                // For now, just fetch all groups by iterating over known group IDs if available
                // groups = ...
                // Simulate with empty list
                groups = emptyList()
            } else {
                error = "Failed to fetch user info."
            }
        } catch (e: Exception) {
            error = e.localizedMessage
        } finally {
            loading = false
        }
    }

    // After creating or joining a group, navigate to genre preferences
    fun onGroupJoinedOrCreated() {
        navController.navigate("genrePreferences")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Groups", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else if (groups.isEmpty()) {
            Text("You are not in any groups yet.")
        } else {
            LazyColumn {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { navController.navigate("groupDetails/${group.id}") }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(group.name, style = MaterialTheme.typography.titleMedium)
                            Text("Invite Code: ${group.inviteCode}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreateDialog = true }) {
                Text("Create Group")
            }
            Button(onClick = { showJoinDialog = true }) {
                Text("Join Group")
            }
        }
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false; groupName = ""; dialogError = null },
                title = { Text("Create Group") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text("Group Name") }
                        )
                        dialogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (groupName.isBlank()) {
                            dialogError = "Group name cannot be empty."
                        } else {
                            scope.launch {
                                try {
                                    val response = api.createGroup(mapOf("name" to groupName))
                                    if (response.isSuccessful) {
                                        showCreateDialog = false
                                        groupName = ""
                                        dialogError = null
                                        onGroupJoinedOrCreated()
                                    } else {
                                        dialogError = "Failed to create group."
                                    }
                                } catch (e: Exception) {
                                    dialogError = e.localizedMessage
                                }
                            }
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false; groupName = ""; dialogError = null }) { Text("Cancel") }
                }
            )
        }
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false; inviteCode = ""; dialogError = null },
                title = { Text("Join Group") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = inviteCode,
                            onValueChange = { inviteCode = it },
                            label = { Text("Invite Code") }
                        )
                        dialogError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (inviteCode.isBlank()) {
                            dialogError = "Invite code cannot be empty."
                        } else {
                            scope.launch {
                                try {
                                    val response = api.joinGroup(mapOf("inviteCode" to inviteCode))
                                    if (response.isSuccessful) {
                                        showJoinDialog = false
                                        inviteCode = ""
                                        dialogError = null
                                        onGroupJoinedOrCreated()
                                    } else {
                                        dialogError = "Failed to join group."
                                    }
                                } catch (e: Exception) {
                                    dialogError = e.localizedMessage
                                }
                            }
                        }
                    }) { Text("Join") }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false; inviteCode = ""; dialogError = null }) { Text("Cancel") }
                }
            )
        }
    }
}
