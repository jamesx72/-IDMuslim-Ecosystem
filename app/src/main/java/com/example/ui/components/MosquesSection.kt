package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.Circle
import com.example.data.LatLng
import com.example.data.LocationRestriction
import com.example.data.MosqueSearchRepository
import com.example.data.Place
import com.example.data.PlacesApiClient
import com.example.data.SearchNearbyRequest
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class CommunityCenterPartner(
    val id: String,
    val name: String,
    val address: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String = "+33 1 40 00 00 00",
    val status: String = "Partenaire Agréé IDMuslim (Niveau 3)"
)

@SuppressLint("MissingPermission")
@Composable
fun MosquesSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { com.example.network.ApiClient.getSessionManager() }

    // Room Database for Search History (5 recent searches)
    val db = remember { AppDatabase.getDatabase(context) }
    val mosqueSearchRepo = remember { MosqueSearchRepository(db.mosqueSearchDao()) }
    val recentSearches by mosqueSearchRepo.recentSearches.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }

    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val placeListType = remember { Types.newParameterizedType(List::class.java, Place::class.java) }
    val placeAdapter = remember { moshi.adapter<List<Place>>(placeListType) }

    var mosques by remember { mutableStateOf<List<Place>?>(null) }
    var configuredMosque by remember { mutableStateOf(sessionManager.getConfiguredMosque()) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isCachedData by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var showMapView by remember { mutableStateOf(false) }

    val authorizedPartners = remember {
        listOf(
            CommunityCenterPartner("1", "Grande Mosquée & Centre Communautaire Central", "2 Bis Rue de la Mosquée, 75005 Paris", "Centre Communautaire & Partenaire d'Identité Agréé", 48.8418, 2.3552, "+33 1 45 35 97 33"),
            CommunityCenterPartner("2", "Espace Communautaire Lyon Métropole", "145 Boulevard Pinel, 69008 Lyon", "Partenaire de Vérification Physique Agréé", 45.7360, 4.8870, "+33 4 78 76 00 23"),
            CommunityCenterPartner("3", "Centre Islamique & Tiers de Confiance Marseille", "8 Rue Saint-Bazile, 13001 Marseille", "Centre Agréé Validation Biométrique", 43.2995, 5.3840, "+33 4 91 50 12 89"),
            CommunityCenterPartner("4", "Maison Communautaire & Culturelle Lille", "30 Rue de Marquillies, 59000 Lille", "Partenaire d'Identité & Guichet Agréé", 50.6200, 3.0450, "+33 3 20 54 88 12")
        )
    }

    // Filtered lists based on search query
    val filteredPartners = remember(searchQuery, authorizedPartners) {
        if (searchQuery.isBlank()) authorizedPartners
        else authorizedPartners.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredMosques = remember(searchQuery, mosques) {
        val list = mosques ?: emptyList()
        if (searchQuery.isBlank()) list
        else list.filter {
            (it.displayName?.text ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.formattedAddress ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    val totalMatches = filteredPartners.size + filteredMosques.size

    val executeSearchAndSave = { query: String ->
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            coroutineScope.launch {
                val matchCount = (mosques ?: emptyList()).count {
                    (it.displayName?.text ?: "").contains(trimmed, ignoreCase = true) ||
                    (it.formattedAddress ?: "").contains(trimmed, ignoreCase = true)
                } + authorizedPartners.count {
                    it.name.contains(trimmed, ignoreCase = true) ||
                    it.address.contains(trimmed, ignoreCase = true) ||
                    it.type.contains(trimmed, ignoreCase = true)
                }
                mosqueSearchRepo.saveSearchQuery(trimmed, matchCount)
            }
        }
    }

    val fusedLocationClient = remember(context) { 
        LocationServices.getFusedLocationProviderClient(context) 
    }

    // Load initial cached mosques if present
    LaunchedEffect(Unit) {
        val cachedJson = sessionManager.getCachedMosques()
        if (!cachedJson.isNullOrEmpty()) {
            try {
                val list = placeAdapter.fromJson(cachedJson)
                if (!list.isNullOrEmpty()) {
                    mosques = list
                    isCachedData = true
                }
            } catch (e: Exception) {
                sessionManager.clearMosqueCache()
            }
        }
    }

    val fetchMosques = { lat: Double, lng: Double ->
        coroutineScope.launch {
            isLoading = true
            try {
                val apiKey = BuildConfig.PLACES_API_KEY
                if (apiKey == "YOUR_GOOGLE_PLACES_API_KEY" || apiKey.isEmpty()) {
                    locationError = "Veuillez configurer PLACES_API_KEY dans les secrets."
                    isLoading = false
                    return@launch
                }
                
                val request = SearchNearbyRequest(
                    includedTypes = listOf("mosque"),
                    maxResultCount = 10,
                    locationRestriction = LocationRestriction(
                        circle = Circle(
                            center = LatLng(lat, lng),
                            radius = 5000.0 // 5km
                        )
                    )
                )
                val response = PlacesApiClient.api.searchNearby(
                    apiKey = apiKey,
                    request = request
                )
                val fetched = response.places ?: emptyList()
                mosques = fetched
                isCachedData = false
                locationError = null

                // Save to local cache
                if (fetched.isNotEmpty()) {
                    sessionManager.saveCachedMosques(placeAdapter.toJson(fetched))
                }
            } catch (e: Exception) {
                // If network fails, keep cached if available
                if (mosques == null) {
                    locationError = "Échec: ${e.localizedMessage}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    val requestLocation = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLocation = location
                    fetchMosques(location.latitude, location.longitude)
                } else if (mosques == null) {
                    locationError = "Position indisponible."
                }
            }.addOnFailureListener {
                if (mosques == null) {
                    locationError = "Erreur de localisation: ${it.message}"
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            requestLocation()
        } else if (mosques == null) {
            locationError = "Permission de localisation refusée."
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val clearCacheAction = {
        sessionManager.clearMosqueCache()
        sessionManager.clearConfiguredMosque()
        mosques = null
        configuredMosque = null
        isCachedData = false
        Toast.makeText(context, "Cache de la mosquée à proximité effacé avec succès.", Toast.LENGTH_SHORT).show()
        requestLocation()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mosque, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lieux & Partenaires Agréés", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row {
                IconButton(
                    onClick = { showMapView = !showMapView }
                ) {
                    Icon(
                        imageVector = if (showMapView) Icons.AutoMirrored.Filled.List else Icons.Default.Map,
                        contentDescription = if (showMapView) "Vue Liste" else "Carte OpenStreetMap",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { requestLocation() },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualiser les lieux", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Search Bar with Submit Action
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            placeholder = { Text("Rechercher une mosquée, ville ou centre...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Recherche", tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer la recherche")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                executeSearchAndSave(searchQuery)
            })
        )

        // Room Search History: Top 5 Recent Searches
        if (recentSearches.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Recherches récentes (Top 5)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                mosqueSearchRepo.clearAllHistory()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            "Effacer tout",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    items(recentSearches.size) { index ->
                        val item = recentSearches[index]
                        val isSelected = searchQuery.equals(item.query, ignoreCase = true)
                        InputChip(
                            selected = isSelected,
                            onClick = {
                                searchQuery = item.query
                                executeSearchAndSave(item.query)
                            },
                            label = {
                                Text(
                                    item.query,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            mosqueSearchRepo.removeSearch(item.id)
                                        }
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Supprimer de l'historique",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Active Search Results Banner
        if (searchQuery.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (totalMatches > 0) "$totalMatches résultat(s) pour \"$searchQuery\"" else "Aucun lieu trouvé pour \"$searchQuery\"",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    TextButton(
                        onClick = { searchQuery = "" },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Réinitialiser", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Mode selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showMapView,
                onClick = { showMapView = false },
                label = { Text("Vue Liste") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = showMapView,
                onClick = { showMapView = true },
                label = { Text("Carte OpenStreetMap") },
                leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }

        // Configured Mosque Card Banner if set
        configuredMosque?.let { (name, address) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Mosquée à proximité configurée",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    if (address.isNotEmpty()) {
                        Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (showMapView) {
            // Interactive osmdroid Map View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            Configuration.getInstance().userAgentValue = ctx.packageName
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(13.0)
                            }
                        },
                        update = { map ->
                            map.overlays.clear()

                            val defaultLat = userLocation?.latitude ?: filteredMosques.firstOrNull()?.location?.latitude ?: 48.8566
                            val defaultLng = userLocation?.longitude ?: filteredMosques.firstOrNull()?.location?.longitude ?: 2.3522
                            map.controller.setCenter(GeoPoint(defaultLat, defaultLng))

                            // Current User Location Marker
                            userLocation?.let { loc ->
                                val userMarker = Marker(map)
                                userMarker.position = GeoPoint(loc.latitude, loc.longitude)
                                userMarker.title = "📍 Votre position actuelle"
                                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                map.overlays.add(userMarker)
                            }

                            // Mosque Markers (filtered)
                            filteredMosques.forEach { place ->
                                val pLat = place.location?.latitude
                                val pLng = place.location?.longitude
                                if (pLat != null && pLng != null) {
                                    val marker = Marker(map)
                                    marker.position = GeoPoint(pLat, pLng)
                                    marker.title = "🕌 ${place.displayName?.text ?: "Mosquée"}"
                                    marker.snippet = place.formattedAddress ?: "Mosquée répertoriée"
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    map.overlays.add(marker)
                                }
                            }

                            // Authorized Community Centers & Verification Partners (filtered)
                            filteredPartners.forEach { partner ->
                                val partnerMarker = Marker(map)
                                partnerMarker.position = GeoPoint(partner.latitude, partner.longitude)
                                partnerMarker.title = "🏛️ ${partner.name}"
                                partnerMarker.snippet = "${partner.type}\n${partner.address}\nTel: ${partner.phone}"
                                partnerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                map.overlays.add(partnerMarker)
                            }

                            map.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Text(
                            "OpenStreetMap (osmdroid)",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            // Clear Mosque Cache Button
            OutlinedButton(
                onClick = clearCacheAction,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Effacer le cache des lieux")
            }

            if (isCachedData && mosques != null && mosques!!.isNotEmpty()) {
                Text(
                    "Données chargées depuis le cache local :",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Section 1: Authorized Community Centers & Verification Partners
            if (filteredPartners.isNotEmpty()) {
                Text(
                    "Centres Communautaires & Partenaires Agréés",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                filteredPartners.forEach { partner ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(partner.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(partner.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(partner.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        partner.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val uri = android.net.Uri.parse("geo:${partner.latitude},${partner.longitude}?q=${android.net.Uri.encode(partner.name)}")
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Impossible d'ouvrir l'application de cartes", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = "Localiser", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Mosques
            Text(
                "Mosquées à proximité (5km)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            } else if (locationError != null && mosques == null) {
                Text(locationError ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { requestLocation() }) {
                    Text("Réessayer")
                }
            } else if (mosques != null) {
                if (filteredMosques.isEmpty()) {
                    Text(
                        if (searchQuery.isNotBlank()) "Aucune mosquée correspondant à '$searchQuery'." else "Aucune mosquée trouvée à proximité.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    filteredMosques.forEach { place ->
                        val placeName = place.displayName?.text ?: "Mosquée inconnue"
                        val placeAddress = place.formattedAddress ?: ""
                        val isConfigured = configuredMosque?.first == placeName

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isConfigured) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(placeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    if (placeAddress.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(placeAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                // Button to set as configured mosque
                                IconButton(
                                    onClick = {
                                        if (isConfigured) {
                                            sessionManager.clearConfiguredMosque()
                                            configuredMosque = null
                                            Toast.makeText(context, "Mosquée configurée retirée", Toast.LENGTH_SHORT).show()
                                        } else {
                                            sessionManager.saveConfiguredMosque(placeName, placeAddress)
                                            configuredMosque = Pair(placeName, placeAddress)
                                            Toast.makeText(context, "'$placeName' configurée comme mosquée de référence !", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isConfigured) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Définir comme mosquée configurée",
                                        tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Directions button
                                IconButton(
                                    onClick = {
                                        val uri = if (place.location != null) {
                                            android.net.Uri.parse("geo:${place.location.latitude},${place.location.longitude}?q=${android.net.Uri.encode(placeName)}")
                                        } else if (placeAddress.isNotEmpty()) {
                                            android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(placeAddress)}")
                                        } else null
                                        
                                        uri?.let {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, it)
                                            intent.setPackage("com.google.android.apps.maps")
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, it))
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "S'y rendre",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
