package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.HapticHelper
import com.example.utils.QiblaCompassManager
import com.google.android.gms.location.LocationServices
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val language by viewModel.language.collectAsState()

    val compassManager = remember(context) { QiblaCompassManager(context) }
    val azimuth by compassManager.azimuth.collectAsState()
    val isSensorAvailable by compassManager.isSensorAvailable.collectAsState()

    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var locationName by remember { mutableStateOf("Position GPS en cours...") }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    val refreshLocation = {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        locationName = "Lat: %.4f, Lng: %.4f".format(location.latitude, location.longitude)
                    } else {
                        // Default fallback
                        userLatitude = 48.8566
                        userLongitude = 2.3522
                        locationName = "Paris, France (Défaut)"
                    }
                }
            } catch (e: SecurityException) {
                userLatitude = 48.8566
                userLongitude = 2.3522
                locationName = "Paris, France (Défaut)"
            }
        } else {
            userLatitude = 48.8566
            userLongitude = 2.3522
            locationName = "Paris, France (Défaut)"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        refreshLocation()
    }

    LaunchedEffect(Unit) {
        compassManager.startListening()
        if (hasLocationPermission) {
            refreshLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    DisposableEffect(compassManager) {
        onDispose {
            compassManager.stopListening()
        }
    }

    // Calculations
    val lat = userLatitude ?: 48.8566
    val lng = userLongitude ?: 2.3522
    val qiblaBearing = remember(lat, lng) {
        QiblaCompassManager.calculateQiblaBearing(lat, lng)
    }
    val distanceKm = remember(lat, lng) {
        QiblaCompassManager.calculateDistanceToKaabaKm(lat, lng)
    }

    // Needle rotation relative to true north / azimuth
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "azimuthAnim"
    )

    // Calculate alignment with Kaaba
    val angleDifference = (qiblaBearing - animatedAzimuth + 360) % 360
    val isAligned = abs(angleDifference) < 4 || abs(angleDifference - 360) < 4

    var previousAlignedState by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !previousAlignedState) {
            HapticHelper.performSuccess(context, haptic)
        }
        previousAlignedState = isAligned
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Boussole Qibla & Kaaba",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Orientation en temps réel vers La Mecque",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshLocation() }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Ma Position")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Alignment Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAligned) Color(0xFF065F46) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Explore,
                            contentDescription = null,
                            tint = if (isAligned) Color(0xFF34D399) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAligned) "Vous êtes face à la Qibla !" else "Tournez l'appareil vers la Kaaba",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Direction Kaaba : ${qiblaBearing.toInt()}° | Cap actuel : ${animatedAzimuth.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAligned) Color(0xFFA7F3D0) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Compass Graphic
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(
                        width = if (isAligned) 4.dp else 2.dp,
                        color = if (isAligned) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .shadow(elevation = 8.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Dial with Cardinal Directions rotating inverse of azimuth
                val dialRotation = -animatedAzimuth

                Canvas(modifier = Modifier.fillMaxSize().rotate(dialRotation)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 24.dp.toPx()

                    // Draw tick marks
                    for (degree in 0 until 360 step 15) {
                        val angleRad = Math.toRadians(degree.toDouble() - 90)
                        val isMajor = degree % 90 == 0
                        val isMinorMajor = degree % 45 == 0
                        val tickLength = if (isMajor) 18.dp.toPx() else if (isMinorMajor) 12.dp.toPx() else 6.dp.toPx()
                        val tickColor = if (isMajor) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f)
                        val strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()

                        val startX = (center.x + (radius - tickLength) * cos(angleRad)).toFloat()
                        val startY = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                        val endX = (center.x + radius * cos(angleRad)).toFloat()
                        val endY = (center.y + radius * sin(angleRad)).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Draw Kaaba Marker Icon / Dot on the dial ring at qiblaBearing
                    val kaabaAngleRad = Math.toRadians(qiblaBearing.toDouble() - 90)
                    val kaabaX = (center.x + (radius - 32.dp.toPx()) * cos(kaabaAngleRad)).toFloat()
                    val kaabaY = (center.y + (radius - 32.dp.toPx()) * sin(kaabaAngleRad)).toFloat()

                    drawCircle(
                        color = Color(0xFFD97706), // Gold / Amber
                        radius = 10.dp.toPx(),
                        center = Offset(kaabaX, kaabaY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(kaabaX, kaabaY)
                    )
                }

                // Cardinal letters inside dial
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                        .rotate(dialRotation)
                ) {
                    Text("N", modifier = Modifier.align(Alignment.TopCenter), fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 16.sp)
                    Text("S", modifier = Modifier.align(Alignment.BottomCenter), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Text("E", modifier = Modifier.align(Alignment.CenterEnd), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Text("O", modifier = Modifier.align(Alignment.CenterStart), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }

                // Fixed Device Indicator (Pointing Forward)
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 6.dp),
                    tint = if (isAligned) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )

                // Central Qibla Pointer Needle
                val needleAngle = qiblaBearing - animatedAzimuth
                Canvas(modifier = Modifier.size(180.dp).rotate(needleAngle)) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // North / Kaaba pointing half (Green / Gold)
                    val topPath = Path().apply {
                        moveTo(center.x, center.y - 70.dp.toPx())
                        lineTo(center.x - 12.dp.toPx(), center.y)
                        lineTo(center.x + 12.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = topPath,
                        color = if (isAligned) Color(0xFF10B981) else Color(0xFF047857)
                    )

                    // Bottom half (Gray)
                    val bottomPath = Path().apply {
                        moveTo(center.x, center.y + 70.dp.toPx())
                        lineTo(center.x - 12.dp.toPx(), center.y)
                        lineTo(center.x + 12.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        path = bottomPath,
                        color = Color.LightGray.copy(alpha = 0.7f)
                    )

                    // Central Pivot
                    drawCircle(color = Color(0xFF0F172A), radius = 10.dp.toPx(), center = center)
                    drawCircle(color = if (isAligned) Color(0xFF34D399) else Color(0xFFD97706), radius = 6.dp.toPx(), center = center)
                }

                // Center Kaaba Emblem
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isAligned) Color(0xFF047857) else Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "Kaaba",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Metrics & Distance info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Angle Qibla", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${qiblaBearing.toInt()}° N", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Distance La Mecque", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "%,d km".format(distanceKm.toInt()), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Position de calcul", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(locationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (!isSensorAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Capteur magnétomètre absent ou non calibré. Déplacez l'appareil en forme de '8' pour calibrer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
