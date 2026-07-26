package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.Circle
import com.example.data.LatLng
import com.example.data.LocationRestriction
import com.example.data.Place
import com.example.data.PlacesApiClient
import com.example.data.SearchNearbyRequest
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MosquesSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { com.example.network.ApiClient.getSessionManager() }

    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val placeListType = remember { Types.newParameterizedType(List::class.java, Place::class.java) }
    val placeAdapter = remember { moshi.adapter<List<Place>>(placeListType) }

    var mosques by remember { mutableStateOf<List<Place>?>(null) }
    var configuredMosque by remember { mutableStateOf(sessionManager.getConfiguredMosque()) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isCachedData by remember { mutableStateOf(false) }

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
                Text("Mosquées à proximité (5km)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { requestLocation() },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualiser les mosquées", tint = MaterialTheme.colorScheme.primary)
            }
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
            Text("Effacer le cache de la mosquée à proximité")
        }

        if (isCachedData && mosques != null && mosques!!.isNotEmpty()) {
            Text(
                "Données chargées depuis le cache local :",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else if (locationError != null && mosques == null) {
            Text(locationError ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { requestLocation() }) {
                Text("Réessayer")
            }
        } else if (mosques != null) {
            if (mosques!!.isEmpty()) {
                Text("Aucune mosquée trouvée à proximité.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                mosques!!.forEach { place ->
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
