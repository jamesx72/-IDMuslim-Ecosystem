package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MosquesSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mosques by remember { mutableStateOf<List<Place>?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val fetchMosques = { lat: Double, lng: Double ->
        coroutineScope.launch {
            isLoading = true
            try {
                val apiKey = BuildConfig.PLACES_API_KEY
                // Check if api key is provided
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
                mosques = response.places ?: emptyList()
                locationError = null
            } catch (e: Exception) {
                locationError = "Échec: ${e.localizedMessage}"
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
                } else {
                    locationError = "Position indisponible."
                }
            }.addOnFailureListener {
                locationError = "Erreur de localisation: ${it.message}"
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            requestLocation()
        } else {
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

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mosquées à proximité (5km)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        } else if (locationError != null) {
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
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(place.displayName?.text ?: "Mosquée inconnue", fontWeight = FontWeight.Bold)
                            if (!place.formattedAddress.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(place.formattedAddress, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
