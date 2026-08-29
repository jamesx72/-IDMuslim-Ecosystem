package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CommunityPostEntity
import com.example.data.EventEntity
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val language by viewModel.language.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var mapFocusEventId by remember { mutableStateOf<Int?>(null) }
    val tabs = listOf(
        Translations.get(language, "tab_events"),
        Translations.get(language, "tab_interactive_map"),
        Translations.get(language, "tab_feed")
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(Translations.get(language, "nav_forum")) },
                    actions = {
                        val currentTheme by viewModel.darkTheme.collectAsState()
                        val isDark = currentTheme == "dark" || (currentTheme == "system" && androidx.compose.foundation.isSystemInDarkTheme())
                        androidx.compose.material3.IconButton(
                            onClick = { viewModel.updateDarkTheme(if (isDark) "light" else "dark") }
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = Translations.get(language, "nav_create_event"))
                }
            } else if (selectedTabIndex == 2) {
                val isVerified = viewModel.isUserVerified.collectAsState().value
                if (isVerified) {
                    var showCreatePostDialog by remember { mutableStateOf(false) }
                    if (showCreatePostDialog) {
                        CreatePostDialog(
                            onDismiss = { showCreatePostDialog = false },
                            onSubmit = { title, content, type ->
                                val sessionManager = com.example.network.ApiClient.getSessionManager()
                                val communityName = sessionManager.getProfileCommunityAffiliation()?.takeIf { it.isNotEmpty() } ?: "Général"
                                viewModel.createCommunityPost(title, content, type, communityName)
                                showCreatePostDialog = false
                            }
                        )
                    }
                    FloatingActionButton(
                        onClick = { showCreatePostDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Créer un post")
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> {
                EventsList(
                    viewModel = viewModel,
                    innerPadding = innerPadding,
                    onNavigateToDetail = onNavigateToDetail,
                    onViewOnMap = { eventId ->
                        mapFocusEventId = eventId
                        selectedTabIndex = 1
                    }
                )
            }
            1 -> {
                CommunityMapScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding),
                    initialSelectedEventId = mapFocusEventId,
                    onNavigateToDetail = onNavigateToDetail
                )
            }
            else -> {
                CommunityFeed(
                    viewModel = viewModel,
                    innerPadding = innerPadding
                )
            }
        }
    }
}

@Composable
fun EventsList(
    viewModel: EventViewModel,
    innerPadding: PaddingValues,
    onNavigateToDetail: (Int) -> Unit,
    onViewOnMap: (Int) -> Unit = {}
) {
    val language by viewModel.language.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    
    if (events.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Aucun événement disponible.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(events) { event ->
                EventItemCard(
                    event = event,
                    language = language,
                    onClick = { onNavigateToDetail(event.id) },
                    onViewOnMap = { onViewOnMap(event.id) }
                )
            }
        }
    }
}

@Composable
fun CommunityFeed(
    viewModel: EventViewModel,
    innerPadding: PaddingValues
) {
    val posts by viewModel.communityPosts.collectAsState()
    val sessionManager = com.example.network.ApiClient.getSessionManager()
    val userCommunity = sessionManager.getProfileCommunityAffiliation() ?: ""
    val filteredPosts = if (userCommunity.isNotEmpty()) {
        posts.filter { it.communityName == userCommunity || it.communityName == "Général" }
    } else {
        posts
    }

    if (filteredPosts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Aucune actualité disponible.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPosts) { post ->
                PostItemCard(post = post, onDelete = { viewModel.deleteCommunityPost(post.id) })
            }
        }
    }
}

@Composable
fun PostItemCard(post: CommunityPostEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.type,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Par: ${post.authorName} (${post.communityName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                // Allow admin to delete post 
                // Using session manager simply for UI
                val isAdmin = com.example.network.ApiClient.getSessionManager().isUserVerified()
                if(isAdmin) {
                    Text(
                        text = "Supprimer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onSubmit: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Information") } // "Information", "Événement", "Don"
    
    val types = listOf("Information", "Événement", "Don")
    var expandedType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle annonce") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Type d'annonce", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.forEach { t ->
                        androidx.compose.material3.FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onSubmit(title, content, type) }
            ) {
                Text("Publier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EventItemCard(
    event: EventEntity,
    language: String,
    onClick: () -> Unit,
    onViewOnMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (event.price == 0.0) "Gratuit" else "${event.price} €",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Organisé par : ${event.organizer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${event.date} à ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "${event.availableTickets} places rest.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewOnMap,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.get(language, "map_view_on_map"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

