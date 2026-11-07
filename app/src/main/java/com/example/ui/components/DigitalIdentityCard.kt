package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.utils.HapticHelper
import com.example.utils.QiblaCompassManager
import com.example.utils.VerificationPortalHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.abs

/**
 * Digital Identity Card UI component that displays:
 * 1. User profile details (Full name, Member ID, Verification badge, residency, community)
 * 2. Scannable QR code for instant verification
 * 3. Qibla-aligned status indicator with real-time compass heading and orientation alignment
 */
@Composable
fun DigitalIdentityCard(
    name: String,
    isVerified: Boolean = true,
    avatarUrl: String? = null,
    userId: String,
    verificationStatus: String = if (isVerified) "VERIFIED" else "PENDING",
    communityAffiliation: String = "IDMuslim Global",
    residency: String = "France",
    dateOfBirth: String = "15/07/1995",
    expiryDate: String = "12/2028",
    userLatitude: Double = 48.8566, // Default Paris
    userLongitude: Double = 2.3522,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var flipped by remember { mutableStateOf(false) }
    var showQrEnlargedDialog by remember { mutableStateOf(false) }
    var showQiblaInfoDialog by remember { mutableStateOf(false) }

    // 3D Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "cardFlipAnimation"
    )

    // Qibla Compass Sensor Manager
    val compassManager = remember { QiblaCompassManager(context) }
    val azimuth by compassManager.azimuth.collectAsState()
    val isSensorAvailable by compassManager.isSensorAvailable.collectAsState()

    DisposableEffect(Unit) {
        compassManager.startListening()
        onDispose {
            compassManager.stopListening()
        }
    }

    // Calculate Qibla angle from user position
    val qiblaBearing = remember(userLatitude, userLongitude) {
        QiblaCompassManager.calculateQiblaBearing(userLatitude, userLongitude)
    }

    val distanceToKaaba = remember(userLatitude, userLongitude) {
        QiblaCompassManager.calculateDistanceToKaabaKm(userLatitude, userLongitude).toInt()
    }

    // Relative angle between phone orientation and Qibla
    val relativeAngle = (azimuth - qiblaBearing + 360) % 360
    val angleDifference = if (relativeAngle > 180) 360 - relativeAngle else relativeAngle
    val isQiblaAligned = angleDifference <= 12f // Within 12 degrees alignment

    // Animated Qibla needle rotation
    val smoothNeedleRotation by animateFloatAsState(
        targetValue = -relativeAngle,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "qiblaNeedle"
    )

    // Aligned aura glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "qiblaAura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    // Generate high-resolution QR code
    val qrBitmap = remember(userId, name, isVerified, primaryColor) {
        try {
            val portalUrl = VerificationPortalHelper.getOrCreateActivePortal(
                memberId = userId,
                fullName = name,
                verificationStatus = verificationStatus
            ).url
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(portalUrl, BarcodeFormat.QR_CODE, 240, 240, hints)
            val bitmap = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
            for (x in 0 until 240) {
                for (y in 0 until 240) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            // Draw Islamic Rub el Hizb symbol in center
            val canvas = Canvas(bitmap)
            val center = 120f
            val radius = 30f

            val bgPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(center, center, radius + 4f, bgPaint)

            val paint = Paint().apply {
                color = 0xFF0D9488.toInt() // Teal emerald
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
                isAntiAlias = true
            }

            val rectSize = radius * 0.72f
            val path = Path().apply {
                addRect(center - rectSize, center - rectSize, center + rectSize, center + rectSize, Path.Direction.CW)
            }
            val path2 = Path().apply {
                addRect(center - rectSize, center - rectSize, center + rectSize, center + rectSize, Path.Direction.CW)
            }

            canvas.save()
            canvas.drawPath(path, paint)
            canvas.rotate(45f, center, center)
            canvas.drawPath(path2, paint)
            canvas.restore()
            canvas.drawCircle(center, center, rectSize * 0.35f, paint)

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clickable {
                HapticHelper.performClick(context, haptic)
                flipped = !flipped
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(
            width = if (isQiblaAligned) 2.dp else 1.2.dp,
            brush = if (isQiblaAligned) {
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF10B981),
                        Color(0xFF34D399),
                        Color(0xFFFFD700),
                        Color(0xFF10B981)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = 0.7f),
                        Color(0xFFD4AF37).copy(alpha = 0.4f)
                    )
                )
            }
        )
    ) {
        if (rotation <= 90f) {
            // ================= FRONT SIDE =================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F2A1D), // Rich Islamic Deep Emerald
                                Color(0xFF061A12),
                                Color(0xFF0D3322)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
                    .drawBehind {
                        // Subtle golden geometric luxury lines
                        val stroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f))
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.08f),
                            radius = size.maxDimension * 0.45f,
                            center = Offset(size.width * 0.85f, size.height * 0.2f),
                            style = stroke
                        )
                    }
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row: Organization Emblem + Qibla-Aligned Status Indicator Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD4AF37).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f)),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Emblème IDMuslim",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "IDMUSLIM",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.6.sp,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "• carte numérique",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFD700).copy(alpha = 0.85f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = communityAffiliation.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 7.5.sp,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        // Qibla-Aligned Status Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isQiblaAligned) {
                                Color(0xFF059669).copy(alpha = 0.35f)
                            } else {
                                Color(0xFF0F172A).copy(alpha = 0.6f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isQiblaAligned) Color(0xFF34D399) else Color(0xFFFFD700).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable {
                                HapticHelper.performClick(context, haptic)
                                showQiblaInfoDialog = true
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                // Rotating Compass Needle Icon
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .graphicsLayer { rotationZ = smoothNeedleRotation },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Boussole Qibla",
                                        tint = if (isQiblaAligned) Color(0xFF34D399) else Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isQiblaAligned) "QIBLA ALIGNÉ" else "QIBLA ${qiblaBearing.toInt()}°",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 7.5.sp,
                                            color = if (isQiblaAligned) Color(0xFF34D399) else Color(0xFFFFEAA7),
                                            letterSpacing = 0.4.sp
                                        )
                                        if (isQiblaAligned) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF34D399).copy(alpha = auraAlpha))
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isQiblaAligned) "Face à La Mecque" else "Cap vers Kaaba",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 6.5.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Middle Section: Avatar Photo + User Profile Details + Scannable QR Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.6.dp,
                                    brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFD4AF37))),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Photo de profil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Avatar par défaut",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // User Details Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = name.ifBlank { "Membre IDMuslim" }.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp
                            )

                            // Verification Status Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isVerified) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isVerified) "Statut : Vérifié Officiel" else "Statut : En Attente",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = if (isVerified) Color(0xFF34D399) else Color(0xFFFBBF24)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column {
                                    Text("RÉSIDENCE", style = MaterialTheme.typography.labelSmall, fontSize = 6.5.sp, color = Color.White.copy(alpha = 0.55f))
                                    Text(residency, style = MaterialTheme.typography.bodySmall, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("EXPIRATION", style = MaterialTheme.typography.labelSmall, fontSize = 6.5.sp, color = Color.White.copy(alpha = 0.55f))
                                    Text(expiryDate, style = MaterialTheme.typography.bodySmall, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFEAA7))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // QR Code Box for Verification
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    HapticHelper.performClick(context, haptic)
                                    showQrEnlargedDialog = true
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "Agrandir le QR Code de vérification",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        tint = Color.Black,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Bar: Member ID & Flip Hint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "N° ID: ",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.5.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = userId,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.5.sp,
                                color = Color(0xFFFFD700),
                                letterSpacing = 0.6.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChangeCircle,
                                contentDescription = "Tourner la carte",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "TOUCHER POUR VERSO",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Holographic watermark layer
                HolographicWatermarkOverlay(
                    memberId = userId,
                    isVerified = isVerified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // ================= BACK SIDE =================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0B192C),
                                Color(0xFF1E3E62),
                                Color(0xFF000000)
                            )
                        )
                    )
                    .graphicsLayer { rotationY = 180f }
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "ENCLAVE SÉCURISÉE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "PORTAIL DE VÉRIFICATION OFFICIEL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            color = Color(0xFFFFD700),
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Middle details
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TITULAIRE", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(name.ifBlank { "Membre IDMuslim" }, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DATE DE NAISSANCE", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(dateOfBirth, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ORIENTATION QIBLA", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("${qiblaBearing.toInt()}° ($distanceToKaaba km de La Mecque)", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SCELLÉ NUMÉRIQUE", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("SHA-256 CONFORME", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Bottom Return Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticHelper.performClick(context, haptic)
                                flipped = false
                            }
                    ) {
                        Text(
                            text = "TOUCHER POUR REVENIR AU RECTO",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                HolographicWatermarkOverlay(
                    memberId = userId,
                    isVerified = isVerified,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                )
            }
        }
    }

    // Modal: Enlarged Verification QR Code
    if (showQrEnlargedDialog && qrBitmap != null) {
        Dialog(onDismissRequest = { showQrEnlargedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vérification Numérique",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scannez pour valider l'identité et le statut en direct",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(2.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(200.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code de vérification",
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ID: $userId",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrEnlargedDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }

    // Modal: Qibla Alignment Details
    if (showQiblaInfoDialog) {
        Dialog(onDismissRequest = { showQiblaInfoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                        Text(
                            text = "Indicateur d'Alignement Qibla",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isQiblaAligned) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.dp,
                            if (isQiblaAligned) Color(0xFF10B981) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isQiblaAligned) "✨ Téléphone parfaitement orienté !" else "Orientez votre téléphone",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isQiblaAligned) Color(0xFF065F46) else Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cap Qibla (La Mecque) : ${qiblaBearing.toInt()}°\nOrientation actuelle : ${azimuth.toInt()}°\nDistance Kaaba : $distanceToKaaba km",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQiblaInfoDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Compris")
                    }
                }
            }
        }
    }
}
