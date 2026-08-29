package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.CommunityMapRepository
import com.example.data.CommunityPlace
import com.example.data.CommunityPlaceType
import com.example.ui.components.MapMarkerHelper
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

enum class MapFilterCategory {
    ALL, MOSQUES, HALAL, EVENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen(
    viewModel: EventViewModel,
    modifier: Modifier = Modifier,
    initialSelectedEventId: Int? = null,
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val language by viewModel.language.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val cachedPlacesFromDb by viewModel.cachedCommunityPlaces.collectAsState()
    val isMapPlacesCached by viewModel.isMapPlacesCached.collectAsState()

    // Location coordinates (defaults to Paris centre or user solar location)
    val sessionManager = remember { com.example.network.ApiClient.getSessionManager() }
    val (savedLat, savedLng, _) = sessionManager.getLastSolarLocation()
    val userLat = if (savedLat != 0.0) savedLat else CommunityMapRepository.DEFAULT_LAT
    val userLng = if (savedLng != 0.0) savedLng else CommunityMapRepository.DEFAULT_LNG

    val repository = remember { CommunityMapRepository() }
    val allPlaces = remember(cachedPlacesFromDb, userLat, userLng, events) {
        if (cachedPlacesFromDb.isNotEmpty()) {
            cachedPlacesFromDb
        } else {
            repository.getPlaces(userLat, userLng, events)
        }
    }

    var selectedFilter by remember { mutableStateOf(MapFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<CommunityPlace?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Filter places based on search query & category chip
    val filteredPlaces = remember(allPlaces, selectedFilter, searchQuery) {
        allPlaces.filter { place ->
            val matchesCategory = when (selectedFilter) {
                MapFilterCategory.ALL -> true
                MapFilterCategory.MOSQUES -> place.type == CommunityPlaceType.MOSQUE
                MapFilterCategory.HALAL -> place.type == CommunityPlaceType.HALAL_RESTAURANT || place.type == CommunityPlaceType.HALAL_MARKET
                MapFilterCategory.EVENTS -> place.type == CommunityPlaceType.COMMUNITY_EVENT
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                place.name.lowercase().contains(q) ||
                        place.address.lowercase().contains(q) ||
                        place.description.lowercase().contains(q) ||
                        place.services.any { it.lowercase().contains(q) }
            }
            matchesCategory && matchesQuery
        }
    }

    // Auto-select event if initialSelectedEventId passed
    LaunchedEffect(initialSelectedEventId, allPlaces) {
        if (initialSelectedEventId != null) {
            val target = allPlaces.find { it.eventId == initialSelectedEventId }
            if (target != null) {
                selectedPlace = target
                selectedFilter = MapFilterCategory.EVENTS
                mapViewInstance?.controller?.animateTo(GeoPoint(target.latitude, target.longitude))
                mapViewInstance?.controller?.setZoom(16.0)
            }
        }
    }

    // Lifecycle handling for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewInstance?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewInstance?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewInstance?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. OSMDroid Map
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                Configuration.getInstance().osmdroidBasePath = ctx.cacheDir
                Configuration.getInstance().osmdroidTileCache = java.io.File(ctx.cacheDir, "osmdroid_tiles")
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(14.5)
                    controller.setCenter(GeoPoint(userLat, userLng))
                    mapViewInstance = this
                }
            },
            update = { mapView ->
                mapViewInstance = mapView
                mapView.overlays.clear()

                // User location marker
                val userMarker = Marker(mapView).apply {
                    position = GeoPoint(userLat, userLng)
                    icon = MapMarkerHelper.createUserLocationDrawable(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = Translations.get(language, "map_my_location")
                }
                mapView.overlays.add(userMarker)

                // Place markers
                filteredPlaces.forEach { place ->
                    val isSelected = selectedPlace?.id == place.id
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(place.latitude, place.longitude)
                        icon = MapMarkerHelper.createMarkerDrawable(context, place.type, isSelected)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = place.name
                        snippet = place.address
                        setOnMarkerClickListener { _, _ ->
                            selectedPlace = place
                            mapView.controller.animateTo(GeoPoint(place.latitude, place.longitude))
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Bar: Search and Category Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
        ) {
            // Search Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = Translations.get(language, "map_search_placeholder"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mosqueCount = allPlaces.count { it.type == CommunityPlaceType.MOSQUE }
                val halalCount = allPlaces.count { it.type == CommunityPlaceType.HALAL_RESTAURANT || it.type == CommunityPlaceType.HALAL_MARKET }
                val eventCount = allPlaces.count { it.type == CommunityPlaceType.COMMUNITY_EVENT }

                CategoryChip(
                    label = Translations.get(language, "map_filter_all"),
                    count = allPlaces.size,
                    iconEmoji = "🌐",
                    isSelected = selectedFilter == MapFilterCategory.ALL,
                    onClick = { selectedFilter = MapFilterCategory.ALL }
                )
                CategoryChip(
                    label = Translations.get(language, "map_filter_mosques"),
                    count = mosqueCount,
                    iconEmoji = "🕌",
                    isSelected = selectedFilter == MapFilterCategory.MOSQUES,
                    onClick = { selectedFilter = MapFilterCategory.MOSQUES }
                )
                CategoryChip(
                    label = Translations.get(language, "map_filter_halal"),
                    count = halalCount,
                    iconEmoji = "🍽️",
                    isSelected = selectedFilter == MapFilterCategory.HALAL,
                    onClick = { selectedFilter = MapFilterCategory.HALAL }
                )
                CategoryChip(
                    label = Translations.get(language, "map_filter_events"),
                    count = eventCount,
                    iconEmoji = "📅",
                    isSelected = selectedFilter == MapFilterCategory.EVENTS,
                    onClick = { selectedFilter = MapFilterCategory.EVENTS }
                )
            }

            // Offline Mode Banner / Offline Cache Status Indicator
            if (!isOnline) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translations.get(language, "map_offline_mode"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Floating Action Controls (Right side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Re-center on User Location
            FloatingActionButton(
                onClick = {
                    mapViewInstance?.controller?.animateTo(GeoPoint(userLat, userLng))
                    mapViewInstance?.controller?.setZoom(15.0)
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = Translations.get(language, "map_my_location"),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Quick Jump to Nearest Mosque
            FloatingActionButton(
                onClick = {
                    val mosques = allPlaces.filter { it.type == CommunityPlaceType.MOSQUE }
                    val nearest = mosques.minByOrNull {
                        CommunityMapRepository.calculateDistanceMeters(userLat, userLng, it.latitude, it.longitude)
                    }
                    if (nearest != null) {
                        selectedPlace = nearest
                        selectedFilter = MapFilterCategory.MOSQUES
                        mapViewInstance?.controller?.animateTo(GeoPoint(nearest.latitude, nearest.longitude))
                        mapViewInstance?.controller?.setZoom(16.0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF059669),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Text("🕌", fontSize = 18.sp)
            }

            // Sync Offline Cache
            FloatingActionButton(
                onClick = {
                    viewModel.refreshMapPlacesCache(userLat, userLng)
                    android.widget.Toast.makeText(
                        context,
                        Translations.get(language, "map_cache_synced"),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = Translations.get(language, "map_sync_cache_btn"),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Zoom In
            FloatingActionButton(
                onClick = { mapViewInstance?.controller?.zoomIn() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(22.dp))
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { mapViewInstance?.controller?.zoomOut() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(22.dp))
            }
        }

        // 4. Selected Place Detail Bottom Card
        AnimatedVisibility(
            visible = selectedPlace != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            selectedPlace?.let { place ->
                val distanceMeters = CommunityMapRepository.calculateDistanceMeters(
                    userLat, userLng, place.latitude, place.longitude
                )
                val distanceText = CommunityMapRepository.formatDistance(distanceMeters)

                PlaceDetailCard(
                    place = place,
                    distanceText = distanceText,
                    language = language,
                    onDismiss = { selectedPlace = null },
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    count: Int,
    iconEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 2.dp,
        shadowElevation = 4.dp,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(iconEmoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailCard(
    place: CommunityPlace,
    distanceText: String,
    language: String,
    onDismiss: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current

    val categoryColor = when (place.type) {
        CommunityPlaceType.MOSQUE -> Color(0xFF059669)
        CommunityPlaceType.HALAL_RESTAURANT -> Color(0xFFD97706)
        CommunityPlaceType.HALAL_MARKET -> Color(0xFFEA580C)
        CommunityPlaceType.COMMUNITY_EVENT -> Color(0xFF2563EB)
    }

    val categoryLabel = when (place.type) {
        CommunityPlaceType.MOSQUE -> Translations.get(language, "map_filter_mosques")
        CommunityPlaceType.HALAL_RESTAURANT -> "Restaurant Halal"
        CommunityPlaceType.HALAL_MARKET -> "Marché & Boucherie Halal"
        CommunityPlaceType.COMMUNITY_EVENT -> Translations.get(language, "map_filter_events")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Category Badge + Verified Shield + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(place.type.iconEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = categoryLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    // Verified Badge
                    if (place.isCertified) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "IDMuslim Shield",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Place Title & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "📍 $distanceText",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Description / Snippet
            if (place.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Opening Hours / Schedule
            place.openingHours?.let { hours ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hours,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Services & Tags (horizontal row)
            if (place.services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    place.services.forEach { service ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "• $service",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Directions Action (Opens Google Maps, Waze, etc.)
                Button(
                    onClick = {
                        val geoUri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}(${Uri.encode(place.name)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.get(language, "map_directions"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Call Button if available
                place.phone?.let { phoneNum ->
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = Translations.get(language, "map_call"),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // If this is a registered community event in the DB, show "Voir Détails" button
                place.eventId?.let { evId ->
                    Button(
                        onClick = { onNavigateToDetail(evId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get(language, "map_details"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        val shareText = "${place.name}\n${place.address}\n${place.openingHours ?: ""}\n📍 https://maps.google.com/?q=${place.latitude},${place.longitude}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, place.name)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, Translations.get(language, "map_share")))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = Translations.get(language, "map_share"),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
