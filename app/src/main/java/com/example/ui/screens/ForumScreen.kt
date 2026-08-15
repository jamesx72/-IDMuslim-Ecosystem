package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CommunityPostEntity
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommentItem(
    val author: String,
    val text: String,
    val time: String = "À l'instant"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(viewModel: EventViewModel) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isVerified by viewModel.isUserVerified.collectAsStateWithLifecycle()
    val posts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val profileCommunity by viewModel.profileCommunityAffiliation.collectAsStateWithLifecycle()
    val profileFullName by viewModel.profileFullName.collectAsStateWithLifecycle()

    var showAddPostDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TOUS") }
    var selectedRegion by remember { mutableStateOf("All Regions") }

    // Dynamic likes and comments state
    var likedPostIds by remember { mutableStateOf(setOf<Int>()) }
    var postLikesCount by remember { mutableStateOf(mapOf<Int, Int>()) }
    var bookmarkedPostIds by remember { mutableStateOf(setOf<Int>()) }
    var activeCommentsPost by remember { mutableStateOf<CommunityPostEntity?>(null) }
    var postCommentsMap by remember {
        mutableStateOf(
            mapOf(
                1 to listOf(
                    CommentItem("Imam Bilal", "Barakallahu feekum pour cette excellente initiative pour les jeunes de la communauté."),
                    CommentItem("Sara M.", "Très utile, je partagerai avec les membres de notre association.")
                )
            )
        )
    }

    val allRegions = remember(posts) {
        listOf("All Regions") + posts.map { it.communityName }.distinct().filter { it.isNotBlank() }.sorted()
    }

    val categories = listOf("TOUS", "INITIATIVE", "EVENT", "DISCUSSION", "ENTRAIDE")

    val filteredPosts = remember(posts, selectedRegion, selectedCategory, searchQuery) {
        posts.filter { post ->
            val matchesRegion = (selectedRegion == "All Regions") || (post.communityName == selectedRegion)
            val matchesCat = (selectedCategory == "TOUS") || (post.type.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    post.title.contains(searchQuery, ignoreCase = true) ||
                    post.content.contains(searchQuery, ignoreCase = true) ||
                    post.authorName.contains(searchQuery, ignoreCase = true)
            matchesRegion && matchesCat && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(Translations.get(language, "forum_title"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Espace d'Échange & Entraide Communautaire", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    val currentTheme by viewModel.darkTheme.collectAsState()
                    val isDark = currentTheme == "dark" || (currentTheme == "system" && androidx.compose.foundation.isSystemInDarkTheme())
                    com.example.ui.components.ThemeToggleAnimatedButton(
                        isDark = isDark,
                        onToggle = { viewModel.updateDarkTheme(if (isDark) "light" else "dark") }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Accès Réservé aux Membres Vérifiés",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Translations.get(language, "forum_auth_required"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un sujet, une entraide ou une annonce...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Region Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allRegions) { region ->
                        SuggestionChip(
                            onClick = { selectedRegion = region },
                            label = { Text(region, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedRegion == region) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (filteredPosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "Aucun message ne correspond à votre recherche" else Translations.get(language, "no_participation"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredPosts) { post ->
                            val isLiked = post.id in likedPostIds
                            val likesCount = (postLikesCount[post.id] ?: 3) + if (isLiked) 1 else 0
                            val isBookmarked = post.id in bookmarkedPostIds
                            val commentsCount = postCommentsMap[post.id]?.size ?: 0

                            EnhancedPostCard(
                                post = post,
                                isLiked = isLiked,
                                likesCount = likesCount,
                                isBookmarked = isBookmarked,
                                commentsCount = commentsCount,
                                onLikeClick = {
                                    likedPostIds = if (isLiked) likedPostIds - post.id else likedPostIds + post.id
                                },
                                onBookmarkClick = {
                                    bookmarkedPostIds = if (isBookmarked) bookmarkedPostIds - post.id else bookmarkedPostIds + post.id
                                    Toast.makeText(context, if (isBookmarked) "Retiré des favoris" else "Enregistré dans vos favoris", Toast.LENGTH_SHORT).show()
                                },
                                onCommentClick = {
                                    activeCommentsPost = post
                                },
                                onShareClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "IDMuslim Communauté:\n${post.title}\n\n${post.content}\n\nPublié par ${post.authorName} (${post.communityName})")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Partager le message"))
                                },
                                onDelete = {
                                    viewModel.deleteCommunityPost(post.id)
                                    Toast.makeText(context, "Message supprimé", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddPostDialog) {
        AddPostDialog(
            language = language,
            defaultCommunity = profileCommunity?.takeIf { it.isNotBlank() } ?: "Grande Mosquée Centrale",
            onDismiss = { showAddPostDialog = false },
            onSubmit = { title, content, type, community ->
                viewModel.createCommunityPost(title, content, type, community)
                showAddPostDialog = false
                Toast.makeText(context, "Publication créée avec succès !", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Comments Sheet Dialog
    activeCommentsPost?.let { post ->
        val currentComments = postCommentsMap[post.id] ?: emptyList()
        var newCommentText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeCommentsPost = null },
            title = {
                Column {
                    Text("Discussion & Réponses", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Sujet: ${post.title.take(30)}...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentComments.isEmpty()) {
                            item {
                                Text("Soyez le premier à participer à cet échange !", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(currentComments) { comment ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(comment.author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                            Text(comment.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(comment.text, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Votre réponse fraternelle...", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    val author = profileFullName?.takeIf { it.isNotBlank() } ?: "Membre Vérifié"
                                    val newComment = CommentItem(author = author, text = newCommentText)
                                    postCommentsMap = postCommentsMap + (post.id to (currentComments + newComment))
                                    newCommentText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeCommentsPost = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun EnhancedPostCard(
    post: CommunityPostEntity,
    isLiked: Boolean,
    likesCount: Int,
    isBookmarked: Boolean,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(post.timestamp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Author + Badge + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                post.authorName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(post.communityName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (post.type.uppercase()) {
                        "INITIATIVE", "ENTRAIDE" -> Color(0xFF059669).copy(alpha = 0.2f)
                        "EVENT" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = post.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (post.type.uppercase()) {
                            "INITIATIVE", "ENTRAIDE" -> Color(0xFF059669)
                            "EVENT" -> Color(0xFF2563EB)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Title & Content
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "J'aime",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        likesCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comments
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Commentaires",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        commentsCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Bookmark
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favoris",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share
                IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
    var type by remember { mutableStateOf("DISCUSSION") }
    var community by remember { mutableStateOf(defaultCommunity) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Créer une Publication",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la publication") },
                    placeholder = { Text("Ex: Initiative panier solidaire...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenu du message") },
                    placeholder = { Text("Détaillez votre annonce, question ou entraide...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                val types = listOf("DISCUSSION", "INITIATIVE", "EVENT", "ENTRAIDE")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = community,
                    onValueChange = { community = it },
                    label = { Text("Mosquée / Organisation de rattachement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSubmit(title, content, type, community)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publier")
                    }
                }
            }
        }
    }
}
