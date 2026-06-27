package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(viewModel: EventViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isVerified by viewModel.isUserVerified.collectAsStateWithLifecycle()
    val posts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val profileCommunity by viewModel.profileCommunityAffiliation.collectAsStateWithLifecycle()

    var showAddPostDialog by remember { mutableStateOf(false) }
    val allRegions = remember(posts) {
        listOf("All Regions") + posts.map { it.communityName }.distinct().filter { it.isNotBlank() }.sorted()
    }
    var selectedRegion by remember { mutableStateOf("All Regions") }
    
    val filteredPosts = remember(posts, selectedRegion) {
        if (selectedRegion == "All Regions") posts else posts.filter { it.communityName == selectedRegion }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get(language, "forum_title"), fontWeight = FontWeight.Bold) },
                actions = {
                    val currentTheme by viewModel.darkTheme.collectAsState()
                    val isDark = currentTheme == "dark" || (currentTheme == "system" && androidx.compose.foundation.isSystemInDarkTheme())
                    IconButton(
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
        },
        floatingActionButton = {
            if (isVerified) {
                FloatingActionButton(
                    onClick = { showAddPostDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = Translations.get(language, "add_post"))
                }
            }
        }
    ) { padding ->
        if (!isVerified) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Translations.get(language, "forum_auth_required"),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = Translations.get(language, "forum_subtitle"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allRegions) { region ->
                        FilterChip(
                            selected = selectedRegion == region,
                            onClick = { selectedRegion = region },
                            label = { Text(region) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (filteredPosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(Translations.get(language, "no_participation"))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPosts) { post ->
                            PostCard(post)
                        }
                    }
                }
            }
        }
    }

    if (showAddPostDialog) {
        AddPostDialog(
            language = language,
            defaultCommunity = profileCommunity ?: "",
            onDismiss = { showAddPostDialog = false },
            onSubmit = { title, content, type, community ->
                viewModel.createCommunityPost(title, content, type, community)
                showAddPostDialog = false
            }
        )
    }
}

@Composable
fun PostCard(post: com.example.data.CommunityPostEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(post.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Par : ${post.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = post.communityName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun AddPostDialog(
    language: String,
    defaultCommunity: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String, type: String, community: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("DISCUSSION") } // DISCUSSION, INITIATIVE, ÉVÉNEMENT
    var community by remember { mutableStateOf(defaultCommunity) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Translations.get(language, "add_post"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(Translations.get(language, "post_title")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(Translations.get(language, "post_content")) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Simple faux-dropdown / segment for type
                val types = listOf("INITIATIVE", "EVENT", "DISCUSSION")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = community,
                    onValueChange = { community = it },
                    label = { Text(Translations.get(language, "community_affiliation")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Translations.get(language, "cancel"))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSubmit(title, content, type, community)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(Translations.get(language, "add_post"))
                    }
                }
            }
        }
    }
}
