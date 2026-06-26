package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.DateInfo
import com.example.data.RetrofitClient
import com.example.data.Timings
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("MissingPermission")
@Composable
fun PrayerTimesSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var prayerTimings by remember { mutableStateOf<Timings?>(null) }
    var dateInfo by remember { mutableStateOf<DateInfo?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val fusedLocationClient = remember(context) { 
        LocationServices.getFusedLocationProviderClient(context) 
    }

    val fetchPrayerTimes = { lat: Double, lng: Double ->
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.getPrayerTimes(lat, lng)
                prayerTimings = response.data?.timings
                dateInfo = response.data?.date
                locationError = null
            } catch (e: Exception) {
                locationError = "Failed to fetch prayer times: ${e.message}"
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
                    fetchPrayerTimes(location.latitude, location.longitude)
                } else {
                    locationError = "Unable to get location."
                }
            }.addOnFailureListener {
                locationError = "Location error: ${it.message}"
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
            locationError = "Location permission denied."
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
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Horaires des Prières (Local)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (locationError != null) {
            Text(locationError ?: "", color = MaterialTheme.colorScheme.error)
            Button(onClick = { requestLocation() }) {
                Text("Réessayer")
            }
        } else if (prayerTimings != null) {
            // Calculate next prayer
            val currentCalendar = Calendar.getInstance()
            val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentCalendar.get(Calendar.MINUTE)
            val currentMinutesTotal = currentHour * 60 + currentMinute
            
            val parseTime = { timeStr: String? ->
                if (timeStr == null) {
                    0
                } else {
                    val timePart = timeStr.substringBefore(" ")
                    val parts = timePart.split(":")
                    if (parts.size >= 2) {
                        val h = parts[0].toIntOrNull() ?: 0
                        val m = parts[1].toIntOrNull() ?: 0
                        h * 60 + m
                    } else 0
                }
            }

            val prayers = listOf(
                "Fajr" to prayerTimings?.Fajr,
                "Dhuhr" to prayerTimings?.Dhuhr,
                "Asr" to prayerTimings?.Asr,
                "Maghrib" to prayerTimings?.Maghrib,
                "Isha" to prayerTimings?.Isha
            )
            
            var nextPrayerName: String? = null
            var nextPrayerMinutesDiff: Int? = null
            var isNow = false
            
            for ((name, timeStr) in prayers) {
                if (timeStr != null) {
                    val timeMins = parseTime(timeStr)
                    if (timeMins >= currentMinutesTotal) {
                        if (timeMins - currentMinutesTotal <= 15 && timeMins - currentMinutesTotal >= 0) {
                            isNow = true
                        }
                        nextPrayerName = name
                        nextPrayerMinutesDiff = timeMins - currentMinutesTotal
                        break
                    }
                }
            }
            
            if (nextPrayerName == null && prayers[0].second != null) {
                nextPrayerName = prayers[0].first
                nextPrayerMinutesDiff = (24 * 60 - currentMinutesTotal) + parseTime(prayers[0].second)
            }
            
            if (nextPrayerName != null && nextPrayerMinutesDiff != null) {
                val hours = nextPrayerMinutesDiff / 60
                val mins = nextPrayerMinutesDiff % 60
                val timeString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            if (isNow || nextPrayerMinutesDiff == 0) {
                                Text(
                                    "C'est l'heure de la prière de $nextPrayerName !",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    "Prochaine prière : $nextPrayerName",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Dans $timeString",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    dateInfo?.let { info ->
                        val hijri = info.hijri
                        if (hijri != null) {
                            Text(
                                text = "${hijri.day} ${hijri.month?.en ?: hijri.month?.ar ?: ""} ${hijri.year} AH",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = info.readable,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    }

                    PrayerRow("Fajr", prayerTimings?.Fajr ?: "", nextPrayerName == "Fajr")
                    PrayerRow("Dhuhr", prayerTimings?.Dhuhr ?: "", nextPrayerName == "Dhuhr")
                    PrayerRow("Asr", prayerTimings?.Asr ?: "", nextPrayerName == "Asr")
                    PrayerRow("Maghrib", prayerTimings?.Maghrib ?: "", nextPrayerName == "Maghrib")
                    PrayerRow("Isha", prayerTimings?.Isha ?: "", nextPrayerName == "Isha")
                }
            }
        }
    }
}

@Composable
fun PrayerRow(name: String, time: String, isNext: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold, color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        Text(time, color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
    }
}
