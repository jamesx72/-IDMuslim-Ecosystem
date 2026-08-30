package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.VerifiedUser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.locales.Translations
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Warning
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.example.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.min

val LocalCardFontScale = compositionLocalOf { 1.0f }

@Composable
fun DigitalIdCard(
    memberId: String,
    isVerified: Boolean,
    verificationStatus: String,
    verificationStep: String,
    profilePhotoBase64: String?,
    cardTheme: Int,
    fullName: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String,
    passportNumber: String? = null,
    licenseNumber: String? = null,
    expiryDate: String,
    language: String,
    privacyMode: Boolean = false,
    onPhotoClick: (() -> Unit)? = null,
    lastSyncTime: Long? = null,
    onDownloadPdfClick: (() -> Unit)? = null,
    onEmergencyClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    isSuspended: Boolean = false,
    isSyncing: Boolean = false,
    isSolarAdaptive: Boolean = false,
    solarState: com.example.utils.SolarState? = null,
    cardFontScale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val effectiveSuspended = isSuspended || 
        verificationStatus.equals("SUSPENDED", ignoreCase = true) || 
        verificationStatus.equals("REVOKED", ignoreCase = true)

    val activeVisualTheme = remember(cardTheme, isSolarAdaptive, solarState) {
        if (isSolarAdaptive && solarState != null && !effectiveSuspended) {
            CardVisualThemes.getThemeById(solarState.adaptedThemeId)
        } else {
            CardVisualThemes.getThemeById(cardTheme)
        }
    }

    val themeColors = if (effectiveSuspended) {
        listOf(Color(0xFF3B0707), Color(0xFF5B1111), Color(0xFF7F1D1D))
    } else if (isSolarAdaptive && solarState != null) {
        solarState.adaptedGradientColors
    } else {
        activeVisualTheme.gradientColors
    }

    val activeAccentColor = if (effectiveSuspended) {
        Color(0xFFEF4444)
    } else if (isSolarAdaptive && solarState != null) {
        solarState.adaptedAccentColor
    } else {
        activeVisualTheme.accentColor
    }

    val isExpiringSoon = remember(expiryDate) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val expDate = sdf.parse(expiryDate)
            if (expDate != null) {
                val diff = expDate.time - System.currentTimeMillis()
                val days = diff / (1000 * 60 * 60 * 24)
                days in 0..30
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // Hologram sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "card_infinite")
    val hologramOffset by infiniteTransition.animateFloat(
        initialValue = -600f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hologram_offset"
    )

    // Gold border shimmer pulse
    val goldBorderShimmer by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gold_border_shimmer"
    )

    // Interactive 3D Flip
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
        ),
        label = "cardFlip3DRotation"
    )
    
    // Dynamic 3D depth scale during flip
    val flipAngleFraction = (rotation % 180f) / 180f
    val depthScale = 1f - (sin(flipAngleFraction * PI.toFloat()) * 0.09f)
    
    // Dynamic elevation lift during flip
    val cardElevation by animateDpAsState(
        targetValue = if (rotation > 15f && rotation < 165f) 28.dp else 16.dp,
        animationSpec = tween(durationMillis = 300),
        label = "cardElevationFlip"
    )
    
    // Interactive touch tilt simulation
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "animatedTiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "animatedTiltY"
    )

    var shieldActive by remember { mutableStateOf(false) }
    var showWebPortalDialog by remember { mutableStateOf(false) }
    var showPhotoZoomDialog by remember { mutableStateOf(false) }
    var showQrEnlargedDialog by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (shieldActive) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "privacyBlur"
    )
    val blurModifier = if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier

    // Deterministic cryptographic hash and NFC UID derived from memberId
    val cryptoHash = remember(memberId) {
        val hashInt = abs(memberId.hashCode().toLong())
        val hex1 = (hashInt and 0xFFFF).toString(16).padStart(4, '0').uppercase()
        val hex2 = ((hashInt shr 16) and 0xFFFF).toString(16).padStart(4, '0').uppercase()
        val hex3 = ((hashInt shr 32) and 0xFFFF).toString(16).padStart(4, '0').uppercase()
        "SHA256:$hex1-$hex2-$hex3"
    }
    
    val nfcChipUid = remember(memberId) {
        val hash = abs(memberId.hashCode())
        val b1 = (hash and 0xFF).toString(16).padStart(2, '0').uppercase()
        val b2 = ((hash shr 8) and 0xFF).toString(16).padStart(2, '0').uppercase()
        val b3 = ((hash shr 16) and 0xFF).toString(16).padStart(2, '0').uppercase()
        val b4 = ((hash shr 24) and 0xFF).toString(16).padStart(2, '0').uppercase()
        "04:$b1:$b2:$b3:$b4"
    }

    // Realistic ICAO 9303 / TD1 Machine Readable Zone (MRZ) formatted 3-line string
    val mrzLines = remember(memberId, fullName, dateOfBirth, expiryDate) {
        val cleanName = fullName.replace(Regex("[^A-Za-z ]"), "").uppercase()
        val nameParts = cleanName.split(" ").filter { it.isNotBlank() }
        val surname = if (nameParts.isNotEmpty()) nameParts.first() else "USER"
        val givenNames = if (nameParts.size > 1) nameParts.drop(1).joinToString("<") else "MUSLIM"
        
        val docNum = memberId.replace("-", "").padEnd(9, '<').take(9).uppercase()
        val dobFormatted = try {
            val parts = dateOfBirth.split("/")
            if (parts.size == 3) "${parts[2].takeLast(2)}${parts[1]}${parts[0]}" else "900101"
        } catch (e: Exception) { "900101" }
        
        val expFormatted = try {
            val parts = expiryDate.split("/")
            if (parts.size == 3) "${parts[2].takeLast(2)}${parts[1]}${parts[0]}" else "300101"
        } catch (e: Exception) { "300101" }

        val line1 = "I<MUS$docNum<<<<<<<<<<<<<<<<".take(30)
        val line2 = "${dobFormatted}4M${expFormatted}2FRA<<<<<<<<<<<9".take(30)
        val line3 = "$surname<<$givenNames<<<<<<<<<<<<<<<<<<<<".take(30)
        listOf(line1, line2, line3)
    }

    val portalInfo = remember(memberId, fullName, verificationStatus, communityAffiliation) {
        com.example.utils.VerificationPortalHelper.getOrCreateActivePortal(
            memberId = memberId,
            fullName = fullName,
            verificationStatus = verificationStatus,
            community = communityAffiliation
        )
    }

    LaunchedEffect(cryptoHash, nfcChipUid, memberId, verificationStatus) {
        val nfcPayload = """
            {
              "id": "$memberId",
              "status": "${verificationStatus.ifEmpty { "VERIFIED" }}",
              "name": "$fullName",
              "dob": "$dateOfBirth",
              "residency": "$residency",
              "community": "$communityAffiliation",
              "nfcUid": "$nfcChipUid",
              "cryptoHash": "$cryptoHash",
              "sig": "$cryptoHash",
              "issuedAt": ${System.currentTimeMillis() / 1000}
            }
        """.trimIndent()
        com.example.nfc.ProfileApduService.activePayload = nfcPayload
    }

    // Decode profile photo
    val decodedBitmap: Bitmap? = remember(profilePhotoBase64) {
        if (profilePhotoBase64 != null) {
            try {
                val decodedString = Base64.decode(profilePhotoBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .aspectRatio(1.586f)
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (effectiveSuspended) Color(0xFFEF4444) else activeAccentColor.copy(alpha = 0.35f),
                spotColor = if (effectiveSuspended) Color(0xFFB91C1C) else activeAccentColor
            )
            .border(
                BorderStroke(
                    width = 1.5.dp,
                    brush = if (effectiveSuspended) {
                        Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFFB91C1C)))
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = goldBorderShimmer),
                                Color(0xFFFFF4D0).copy(alpha = 0.95f),
                                activeAccentColor,
                                Color(0xFFD4AF37).copy(alpha = goldBorderShimmer)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    }
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .graphicsLayer {
                rotationY = rotation + animatedTiltY
                rotationX = -animatedTiltX
                cameraDistance = 16f * density
                scaleX = depthScale
                scaleY = depthScale
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tiltY = (tiltY + dragAmount.x * 0.08f).coerceIn(-12f, 12f)
                        tiltX = (tiltX + dragAmount.y * 0.08f).coerceIn(-12f, 12f)
                    },
                    onDragEnd = {
                        tiltX = 0f
                        tiltY = 0f
                    },
                    onDragCancel = {
                        tiltX = 0f
                        tiltY = 0f
                    }
                )
            },
        onClick = { 
            HapticHelper.performCardFlip(context, haptic)
            isFlipped = !isFlipped
        },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = themeColors,
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
                .drawBehind {
                    // Golden specular light sweep
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFEAA7).copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.28f),
                                Color(0xFFFFEAA7).copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            start = Offset(hologramOffset, 0f),
                            end = Offset(hologramOffset + 450f, size.height)
                        ),
                        start = Offset(hologramOffset, 0f),
                        end = Offset(hologramOffset + 450f, size.height),
                        strokeWidth = 260f
                    )
                    
                    // Subtle micro-guilloche geometric grid
                    val gridSize = 36f
                    for (x in 0..(size.width / gridSize).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.035f),
                            start = Offset(x * gridSize, 0f),
                            end = Offset(x * gridSize, size.height),
                            strokeWidth = 0.75f
                        )
                    }
                    for (y in 0..(size.height / gridSize).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.035f),
                            start = Offset(0f, y * gridSize),
                            end = Offset(size.width, y * gridSize),
                            strokeWidth = 0.75f
                        )
                    }

                    // Security dual concentric watermark rings
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.04f),
                        radius = size.height * 0.48f,
                        center = Offset(size.width * 0.45f, size.height * 0.5f),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.025f),
                        radius = size.height * 0.38f,
                        center = Offset(size.width * 0.45f, size.height * 0.5f),
                        style = Stroke(width = 1.5f)
                    )
                }
        ) {
            // Suspended / Revoked red wash overlay
            if (effectiveSuspended) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFDC2626).copy(alpha = 0.28f))
                )
            }

            // Islamic Art Motif
            IslamicCardPatternBackground(
                themeIndex = activeVisualTheme.id,
                modifier = Modifier.fillMaxSize(),
                alphaMultiplier = if (effectiveSuspended) 0.35f else 1.0f
            )

            CompositionLocalProvider(LocalCardFontScale provides cardFontScale) {
                if (rotation <= 90f) {
                    // ================= FRONT SIDE =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header Bar: Emblem, Title & Quick Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Golden Crescent & Star Seal
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFD4AF37).copy(alpha = 0.22f),
                                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = "IDMuslim Seal",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "IDMUSLIM",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                letterSpacing = 1.8.sp,
                                                fontSize = (13f * cardFontScale).coerceIn(11f, 15f).sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "بطاقة الهوية",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFFD700).copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Text(
                                            text = "OFFICIAL DIGITAL IDENTITY CREDENTIAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.65f),
                                            letterSpacing = 1.1.sp,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Interactive Pill Actions
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Live Background Sync Pill Indicator
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isSyncing,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "cardSyncRotation")
                                    val syncAngle by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "cardSyncAngle"
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.28f),
                                        border = BorderStroke(0.8.dp, Color(0xFF34D399).copy(alpha = 0.8f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = "Synchronisation en direct",
                                                tint = Color(0xFF34D399),
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .graphicsLayer { rotationZ = syncAngle }
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "SYNC",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF34D399),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                // Privacy Shield Toggle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (shieldActive) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                                    border = BorderStroke(0.8.dp, if (shieldActive) Color(0xFF34D399) else Color.White.copy(alpha = 0.25f)),
                                    modifier = Modifier.clickable {
                                        HapticHelper.performPrivacyShieldToggle(context, haptic)
                                        shieldActive = !shieldActive
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (shieldActive) Icons.Default.Shield else Icons.Default.Visibility,
                                            contentDescription = "Shield",
                                            tint = if (shieldActive) Color(0xFF34D399) else Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (shieldActive) "MASQUÉ" else "VISIBLE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (shieldActive) Color(0xFF34D399) else Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // 3D Flip Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.18f),
                                    border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.45f)),
                                    modifier = Modifier.clickable {
                                        HapticHelper.performCardFlip(context, haptic)
                                        isFlipped = true
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChangeCircle,
                                            contentDescription = "3D Flip",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "VERSO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFD700),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Middle Section: Photo & Identity Name & Microchip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Column: User Full Name + Microchip + Verification Badge
                            Column(modifier = Modifier.weight(1f)) {
                                val nameFontSize = (17f * cardFontScale).coerceIn(14f, 20f).sp
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (fullName.isNotBlank()) fullName.uppercase() else Translations.get(language, "user").uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = nameFontSize),
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f, fill = false).then(blurModifier),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    DigitalVerifiedBadge(
                                        isVerified = isVerified,
                                        memberId = memberId,
                                        fullName = fullName,
                                        isSuspended = effectiveSuspended,
                                        size = BadgeSize.SMALL
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Realistic Smartcard ISO Microchip & Contactless Wave
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IsoSmartChip(
                                        modifier = Modifier.size(width = 34.dp, height = 25.dp)
                                    )
                                    
                                    // Contactless NFC Wave Icon
                                    Icon(
                                        imageVector = Icons.Default.Nfc,
                                        contentDescription = "Contactless Smart Card",
                                        tint = Color(0xFFFFD700).copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
                                    )

                                    if (communityAffiliation.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = communityAffiliation.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFEAA7),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Profile Photo Frame with Gold Borders & Tap-to-Zoom
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = 1.8.dp,
                                        brush = if (effectiveSuspended) {
                                            SolidColor(Color(0xFFEF4444))
                                        } else {
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFFD700), Color(0xFFFFF4D0), Color(0xFFD4AF37))
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        HapticHelper.performClick(context, haptic)
                                        if (decodedBitmap != null) {
                                            showPhotoZoomDialog = true
                                        } else {
                                            onPhotoClick?.invoke()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (decodedBitmap != null) {
                                    Image(
                                        bitmap = decodedBitmap.asImageBitmap(),
                                        contentDescription = "Photo d'identité",
                                        modifier = Modifier.fillMaxSize().then(blurModifier),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Ajouter Photo",
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Islamic Shahada Ribbon Bar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF000000).copy(alpha = 0.22f),
                            border = BorderStroke(0.6.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "أَشْهَدُ أَنْ لَا إِلَٰهَ إِلَّا ٱللَّٰهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ ٱللَّٰهِ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFEAA7).copy(alpha = 0.95f),
                                textAlign = TextAlign.Center,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Bottom Data Grid + Mini QR Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    IdField(
                                        label = Translations.get(language, "date_of_birth"),
                                        value = if (dateOfBirth.isNotBlank()) dateOfBirth else "--/--/----",
                                        modifier = blurModifier
                                    )
                                    IdField(
                                        label = Translations.get(language, "residence"),
                                        value = if (residency.isNotBlank()) residency else Translations.get(language, "not_specified"),
                                        modifier = blurModifier
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    IdField(
                                        label = Translations.get(language, "id_number"),
                                        value = memberId,
                                        isMonospace = true,
                                        textColor = Color(0xFFFFD700)
                                    )
                                    IdField(
                                        label = Translations.get(language, "expiry_date"),
                                        value = expiryDate,
                                        isMonospace = true,
                                        textColor = if (isExpiringSoon) Color(0xFFFF6B6B) else Color.White
                                    )
                                }
                            }

                            // Dynamic Mini QR
                            val smallQrBitmap = remember(portalInfo.url) {
                                try {
                                    val bitMatrix = QRCodeWriter().encode(portalInfo.url, BarcodeFormat.QR_CODE, 200, 200)
                                    val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565)
                                    for (x in 0 until 200) {
                                        for (y in 0 until 200) {
                                            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                                        }
                                    }
                                    bitmap.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable {
                                        HapticHelper.performClick(context, haptic)
                                        showQrEnlargedDialog = true
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (smallQrBitmap != null) {
                                        Image(
                                            bitmap = smallQrBitmap,
                                            contentDescription = "Agrandir le QR Code",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = "QR Code",
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ================= BACK SIDE (SECURITY & MRZ) =================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .graphicsLayer { rotationY = 180f }
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
                                    color = Color(0xFF10B981).copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = "Crypto Chip",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ENCLAVE MATÉRIELLE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF34D399),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.6.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PROFIL CRYPTOGRAPHIQUE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                )
                            }

                            // Return Flip Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable {
                                    HapticHelper.performCardFlip(context, haptic)
                                    isFlipped = false
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChangeCircle,
                                        contentDescription = "Retour Recto",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "RECTO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Middle Security Matrix: Hash + NFC UID + QR Portal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1.2f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // SHA-256 Copyable Hash
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Hash", cryptoHash))
                                        Toast.makeText(context, "Hash copié !", Toast.LENGTH_SHORT).show()
                                        HapticHelper.performClick(context, haptic)
                                    }
                                ) {
                                    Column {
                                        Text(
                                            text = "SIGNATURE SHA-256",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            letterSpacing = 0.6.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = cryptoHash,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6EE7B7)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = Color.White.copy(alpha = 0.5f),
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }

                                // NFC UID & Protocol
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column {
                                        Text(
                                            text = "NFC CHIP UID",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = nfcChipUid,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.5.sp,
                                            color = Color.White
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "CHIFFREMENT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 7.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = "AES-256-GCM",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.5.sp,
                                            color = Color(0xFF93C5FD)
                                        )
                                    }
                                }

                                // Passport / License numbers
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IdField(
                                        label = Translations.get(language, "passport_number"),
                                        value = passportNumber?.ifEmpty { "--" } ?: "--",
                                        isMonospace = true,
                                        modifier = blurModifier,
                                        fontSize = 10.sp
                                    )
                                    IdField(
                                        label = Translations.get(language, "license_number"),
                                        value = licenseNumber?.ifEmpty { "--" } ?: "--",
                                        isMonospace = true,
                                        modifier = blurModifier,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Large QR Code
                            val qrBitmap = remember(portalInfo.url) {
                                try {
                                    val bitMatrix = QRCodeWriter().encode(portalInfo.url, BarcodeFormat.QR_CODE, 260, 260)
                                    val bitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.RGB_565)
                                    for (x in 0 until 260) {
                                        for (y in 0 until 260) {
                                            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                                        }
                                    }
                                    bitmap.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                    shadowElevation = 5.dp,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable {
                                            HapticHelper.performClick(context, haptic)
                                            showWebPortalDialog = true
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (qrBitmap != null) {
                                            Image(
                                                bitmap = qrBitmap,
                                                contentDescription = "Portail de vérification",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.QrCode2,
                                                contentDescription = "QR Code",
                                                tint = Color.Black,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "VÉRIFIER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Official Machine Readable Zone (MRZ 3-Lines)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF000000).copy(alpha = 0.4f),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                mrzLines.forEach { line ->
                                    Text(
                                        text = line,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        letterSpacing = 1.4.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suspended Stamp Overlay
            if (effectiveSuspended) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E0303).copy(alpha = 0.62f))
                        .graphicsLayer {
                            if (rotation > 90f) {
                                rotationY = 180f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .graphicsLayer { rotationZ = -6f },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF991B1B).copy(alpha = 0.95f),
                        border = BorderStroke(2.dp, Color(0xFFEF4444)),
                        shadowElevation = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Suspended Warning",
                                tint = Color(0xFFFEE2E2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SUSPENDED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (language == "fr") "COMPTE RÉVOQUÉ PAR L'ADMINISTRATION" else "ACCOUNT REVOKED BY ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 7.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Holographic Watermark Overlay
            HolographicWatermarkOverlay(
                memberId = memberId,
                isVerified = isVerified,
                isSuspended = effectiveSuspended,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (rotation > 90f) {
                            rotationY = 180f
                        }
                    }
            )
        }
    }

    // Photo Zoom Dialog
    if (showPhotoZoomDialog && decodedBitmap != null) {
        Dialog(onDismissRequest = { showPhotoZoomDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PORTRAIT BIOMÉTRIQUE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(onClick = { showPhotoZoomDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = "Photo d'identité haute résolution",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = fullName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: $memberId",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Enlarged QR Code Dialog
    if (showQrEnlargedDialog) {
        val largeQr = remember(portalInfo.url) {
            try {
                val bitMatrix = QRCodeWriter().encode(portalInfo.url, BarcodeFormat.QR_CODE, 500, 500)
                val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565)
                for (x in 0 until 500) {
                    for (y in 0 until 500) {
                        bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                }
                bitmap.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        Dialog(onDismissRequest = { showQrEnlargedDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CODE QR D'AUTHENTIFICATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(onClick = { showQrEnlargedDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(2.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(240.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (largeQr != null) {
                                Image(
                                    bitmap = largeQr,
                                    contentDescription = "QR Code agrandi",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Scannez pour valider l'authenticité",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = portalInfo.url,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (showWebPortalDialog) {
        WebVerificationPortalDialog(
            memberId = memberId,
            fullName = fullName,
            verificationStatus = verificationStatus,
            communityAffiliation = communityAffiliation,
            dateOfBirth = dateOfBirth,
            residency = residency,
            photoBase64 = profilePhotoBase64,
            language = language,
            onDismiss = { showWebPortalDialog = false }
        )
    }
}

/**
 * Realistic ISO/IEC 7816 Smart Card Contact Microchip with Golden Circuit Etchings
 */
@Composable
fun IsoSmartChip(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cornerRadius = CornerRadius(6f, 6f)

        // Chip Gold Substrate
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFDF73), Color(0xFFD4AF37), Color(0xFFAA8010)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            cornerRadius = cornerRadius
        )

        // Outer Metallic Bevel
        drawRoundRect(
            color = Color(0xFF805B00),
            style = Stroke(width = 1.2f),
            cornerRadius = cornerRadius
        )

        // Internal Circuit Division Lines
        val linePaint = Color(0xFF5E4300).copy(alpha = 0.75f)
        val stroke = Stroke(width = 1f)

        // Horizontal middle line
        drawLine(
            color = linePaint,
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.2f
        )

        // Vertical lines
        drawLine(
            color = linePaint,
            start = Offset(w * 0.35f, 0f),
            end = Offset(w * 0.35f, h),
            strokeWidth = 1.2f
        )
        drawLine(
            color = linePaint,
            start = Offset(w * 0.65f, 0f),
            end = Offset(w * 0.65f, h),
            strokeWidth = 1.2f
        )

        // Center contact square
        drawRoundRect(
            color = Color(0xFFFFF1A8),
            topLeft = Offset(w * 0.35f, h * 0.3f),
            size = Size(w * 0.3f, h * 0.4f),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 1f)
        )
    }
}

@Composable
fun IdField(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    textColor: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit? = null,
    modifier: Modifier = Modifier
) {
    val fontScale = LocalCardFontScale.current
    val labelFontSize = (7.5f * fontScale).coerceIn(6.5f, 10f).sp
    val valueFontSize = fontSize ?: (12f * fontScale).coerceIn(10f, 16f).sp

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = (1.1f * fontScale.coerceAtMost(1.15f)).sp,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height((1f * fontScale.coerceAtMost(1.2f)).dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isMonospace) FontWeight.Normal else FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = textColor,
            fontSize = valueFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DigitalIdCardSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(
                    colors = listOf(Color(0xFF374151), Color(0xFF1F2937))
                ))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.White.copy(alpha = shimmerAlpha)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.height(14.dp).width(120.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.height(26.dp).width(160.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(modifier = Modifier.height(12.dp).width(50.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.height(16.dp).width(90.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = shimmerAlpha)))
                    }
                }
            }
        }
    }
}
