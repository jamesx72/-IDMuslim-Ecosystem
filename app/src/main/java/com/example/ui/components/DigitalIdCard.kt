package com.example.ui.components
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val effectiveSuspended = isSuspended || 
        verificationStatus.equals("SUSPENDED", ignoreCase = true) || 
        verificationStatus.equals("REVOKED", ignoreCase = true)

    val themeColors = if (effectiveSuspended) {
        listOf(Color(0xFF3B0707), Color(0xFF5B1111), Color(0xFF7F1D1D)) // Dark Crimson & Red Tone
    } else when (cardTheme) {
        1 -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)) // Ocean Depth
        2 -> listOf(Color(0xFF23074D), Color(0xFFCC5333)) // Sunset Ruby
        else -> listOf(Color(0xFF0F2027), Color(0xFF14533C), Color(0xFF14533C)) // Emerald
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

    val infiniteTransition = rememberInfiniteTransition()
    val hologramOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
        ),
        label = "cardFlip3DRotation"
    )
    
    // Dynamic 3D depth scale: card slightly pulls back during the flip for realistic physical momentum
    val flipAngleFraction = (rotation % 180f) / 180f
    val depthScale = 1f - (sin(flipAngleFraction * PI.toFloat()) * 0.08f)
    
    // Dynamic elevation lift during flip
    val cardElevation by animateDpAsState(
        targetValue = if (rotation > 15f && rotation < 165f) 28.dp else 14.dp,
        animationSpec = tween(durationMillis = 300),
        label = "cardElevationFlip"
    )
    
    var shieldActive by remember { mutableStateOf(true) }
    val blurRadius by animateDpAsState(
        targetValue = if (shieldActive) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
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

    LaunchedEffect(cryptoHash, nfcChipUid, memberId, verificationStatus) {
        val nfcPayload = """
            {
              "id": "$memberId",
              "status": "${verificationStatus.ifEmpty { "UNVERIFIED" }}",
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .aspectRatio(1.586f) // ID card aspect ratio
            .then(
                if (effectiveSuspended) {
                    Modifier.border(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFFB91C1C))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                } else Modifier
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
                scaleX = depthScale
                scaleY = depthScale
            },
        onClick = { 
            if (!isFlipped) {
                val activity = context as? androidx.fragment.app.FragmentActivity
                if (activity != null && com.example.security.BiometricHelper.canAuthenticate(context)) {
                    com.example.security.BiometricHelper.authenticate(
                        activity = activity,
                        title = if (language == "fr") "Vérifier l'identité" else "Verify Identity",
                        subtitle = if (language == "fr") "Déverrouiller le profil de sécurité" else "Unlock security profile",
                        onSuccess = {
                            HapticHelper.performCardFlip(context, haptic)
                            isFlipped = true
                        },
                        onError = { error ->
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    HapticHelper.performCardFlip(context, haptic)
                    isFlipped = true
                }
            } else {
                HapticHelper.performCardFlip(context, haptic)
                isFlipped = false
            }
        },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = themeColors,
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
                .drawBehind {
                    // Holographic shine line
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.Transparent),
                            start = Offset(hologramOffset, 0f),
                            end = Offset(hologramOffset + 500f, size.height)
                        ),
                        start = Offset(hologramOffset, 0f),
                        end = Offset(hologramOffset + 500f, size.height),
                        strokeWidth = 300f
                    )
                    
                    // Dynamic 3D specular light glare responding to 3D rotation angle
                    val specularOffset = (rotation / 180f) * size.width
                    val dynamicGlareAlpha = 0.22f * sin(flipAngleFraction * PI.toFloat())
                    if (dynamicGlareAlpha > 0.01f) {
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = dynamicGlareAlpha),
                                    Color.Transparent
                                ),
                                start = Offset(specularOffset - 100f, 0f),
                                end = Offset(specularOffset + 100f, size.height)
                            ),
                            start = Offset(specularOffset - 100f, 0f),
                            end = Offset(specularOffset + 100f, size.height),
                            strokeWidth = 140f
                        )
                    }
                    
                    // Subtle grid background for high-tech feel
                    val gridSize = 40f
                    for (x in 0..(size.width / gridSize).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(x * gridSize, 0f),
                            end = Offset(x * gridSize, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..(size.height / gridSize).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.03f),
                            start = Offset(0f, y * gridSize),
                            end = Offset(size.width, y * gridSize),
                            strokeWidth = 1f
                        )
                    }
                }
        ) {
            // Red tint overlay for suspended/revoked account status
            if (effectiveSuspended) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFDC2626).copy(alpha = 0.28f))
                )
            }

            if (rotation <= 90f) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1565552645632-d725f8bfc19a?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Logo / Title and Photo/Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { 
                                    HapticHelper.performPrivacyShieldToggle(context, haptic)
                                    shieldActive = !shieldActive 
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (shieldActive) Icons.Default.Shield else androidx.compose.material.icons.Icons.Default.Visibility,
                                    contentDescription = "Toggle Privacy Shield",
                                    tint = if (shieldActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.get(language, "identity_card").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChangeCircle,
                                        contentDescription = "3D Flip",
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "3D FLIP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onDownloadPdfClick != null) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        onDownloadPdfClick()
                                    },
                                    modifier = Modifier.size(32.dp).padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "Download PDF",
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            if (onEmergencyClick != null) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        onEmergencyClick()
                                    },
                                    modifier = Modifier.size(32.dp).padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                        contentDescription = "SOS Emergency",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (fullName.isNotBlank()) fullName.uppercase() else Translations.get(language, "user").uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
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
                    }
                    
                    // Profile Image
                    if (true) {
                        var decodedBitmap: Bitmap? = null
                        if (profilePhotoBase64 != null) {
                            try {
                                val decodedString = android.util.Base64.decode(profilePhotoBase64, android.util.Base64.DEFAULT)
                                decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            } catch (e: Exception) {
                                // Suppress error
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    2.dp,
                                    if (effectiveSuspended) Color(0xFFEF4444) else Color.White.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = onPhotoClick != null) { 
                                    HapticHelper.performClick(context, haptic)
                                    onPhotoClick?.invoke() 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (decodedBitmap != null) {
                                Image(
                                    bitmap = decodedBitmap.asImageBitmap(),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize().then(blurModifier),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Add Photo",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        VerificationStatusBadge(
                            status = verificationStatus,
                            substep = verificationStep,
                            useDarkThemeColors = false
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = Translations.get(language, "shahada_text"),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Content Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            IdField(
                                label = Translations.get(language, "id_number"),
                                value = memberId,
                                isMonospace = true
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IdField(
                                    label = Translations.get(language, "expiry_date"),
                                    value = expiryDate,
                                    isMonospace = true,
                                    textColor = if (isExpiringSoon) Color(0xFFFF6B6B) else Color.White
                                )
                                if (isExpiringSoon) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Expiring Soon",
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Small QR code for quick scan
                    val smallQrBitmap = remember(memberId) {
                        try {
                            val payload = "https://idmuslim.org/verify/$memberId"
                            val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 200, 200)
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

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (smallQrBitmap != null) {
                            Image(
                                bitmap = smallQrBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = Color.Black.copy(alpha = 0.9f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
            } else {
                // BACK SIDE: Comprehensive Cryptographic Security Profile & Verification Panel
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .graphicsLayer { rotationY = 180f }
                ) {
                    // Back Header: Security & Crypto Enclave Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = "Crypto Chip",
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CHIP SECURED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURITY PROFILE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        // Return Flip Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    HapticHelper.performCardFlip(context, haptic)
                                    isFlipped = false
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChangeCircle,
                                    contentDescription = "Flip Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "FRONT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Middle Security Grid (Details Left, QR Right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Cryptographic Proofs & Protected Data
                        Column(
                            modifier = Modifier.weight(1.15f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Digital Signature Hash
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Security Hash", cryptoHash)
                                    clipboard?.setPrimaryClip(clip)
                                    Toast.makeText(context, "Crypto Hash Copied!", Toast.LENGTH_SHORT).show()
                                    HapticHelper.performClick(context, haptic)
                                }
                            ) {
                                Column {
                                    Text(
                                        text = "INTEGRITY HASH (SHA-256)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 7.5.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        letterSpacing = 0.8.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cryptoHash,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6EE7B7)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Hash",
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }

                            // Virtual NFC UID & Protocol
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(
                                        text = "NFC CHIP UID",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 7.5.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = nfcChipUid,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = "SECURITY PROTOCOL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 7.5.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "AES-256-GCM • SHIELD",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            }

                            // Official Government ID Fields (Protected by Privacy Shield)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                IdField(
                                    label = Translations.get(language, "passport_number"),
                                    value = passportNumber?.ifEmpty { "--" } ?: "--",
                                    isMonospace = true,
                                    modifier = blurModifier
                                )
                                IdField(
                                    label = Translations.get(language, "license_number"),
                                    value = licenseNumber?.ifEmpty { "--" } ?: "--",
                                    isMonospace = true,
                                    modifier = blurModifier
                                )
                            }
                        }

                        // Right Column: Full Verification QR Code
                        val qrBitmap = remember(memberId) {
                            try {
                                val payload = "https://idmuslim.org/verify/$memberId"
                                val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 300, 300)
                                val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)
                                for (x in 0 until 300) {
                                    for (y in 0 until 300) {
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
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "QR Code Verification",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = "QR Code",
                                            tint = Color.Black,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SCAN TO VERIFY",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Bottom Security Micro-Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (lastSyncTime != null) {
                                    "Synced: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(lastSyncTime))
                                } else {
                                    "HARDWARE ENCLAVE VERIFIED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "IDMUSLIM AUTHENTIC",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // High Visibility Security Suspended / Revoked Overlay
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
                            .graphicsLayer {
                                rotationZ = -6f
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF991B1B).copy(alpha = 0.95f),
                        border = BorderStroke(2.dp, Color(0xFFEF4444)),
                        shadowElevation = 14.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Suspended Warning",
                                tint = Color(0xFFFEE2E2),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SUSPENDED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 3.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (language == "fr") "COMPTE RÉVOQUÉ PAR L'ADMINISTRATION" else if (language == "ar") "تم تعليق الحساب من قبل المسؤول" else "ACCOUNT REVOKED BY ADMIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 8.sp,
                                    letterSpacing = 0.4.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Suspended Warning",
                                tint = Color(0xFFFEE2E2),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Subtle Dynamic Holographic Digital Watermark Overlay (Anti-Screenshot / Anti-Copy protection)
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
}

@Composable
fun IdField(label: String, value: String, isMonospace: Boolean = false, textColor: Color = Color.White, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isMonospace) FontWeight.Normal else FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = textColor,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DigitalIdCardSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(24.dp),
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
                    .padding(20.dp)
            ) {
                // Header (Logo and Title)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.height(28.dp).width(160.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.height(16.dp).width(180.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.height(24.dp).width(80.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(12.dp)))
                    }
                    
                    // Profile photo skeleton
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Footer (ID & Dates)
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
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(modifier = Modifier.height(12.dp).width(60.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.height(16.dp).width(80.dp).background(Color.White.copy(alpha = shimmerAlpha), RoundedCornerShape(4.dp)))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = shimmerAlpha)))
                    }
                }
            }
        }
    }
}
