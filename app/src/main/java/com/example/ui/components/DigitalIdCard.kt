package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
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
    modifier: Modifier = Modifier
) {
    val themeColors = when (cardTheme) {
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
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .aspectRatio(1.586f) // ID card aspect ratio
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        onClick = { isFlipped = !isFlipped },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
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
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.get(language, "identity_card").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (privacyMode) Translations.get(language, "hidden_field") else if (fullName.isNotBlank()) fullName.uppercase() else Translations.get(language, "user").uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isVerified) {
                                DigitalVerifiedBadge(
                                    isVerified = true,
                                    memberId = memberId,
                                    fullName = fullName,
                                    size = BadgeSize.SMALL
                                )
                            }
                        }
                    }
                    
                    // Profile Image
                    if (profilePhotoBase64 != null && !privacyMode) {
                        var decodedBitmap: Bitmap? = null
                        try {
                            val decodedString = android.util.Base64.decode(profilePhotoBase64, android.util.Base64.DEFAULT)
                            decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            // Suppress error
                        }
                        if (decodedBitmap != null) {
                            Image(
                                bitmap = decodedBitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
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
                                value = if (privacyMode) Translations.get(language, "hidden_field") else if (dateOfBirth.isNotBlank()) dateOfBirth else "--/--/----"
                            )
                            IdField(
                                label = Translations.get(language, "residence"),
                                value = if (privacyMode) Translations.get(language, "hidden_field") else if (residency.isNotBlank()) residency else Translations.get(language, "not_specified")
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
                    
                    // Decorative/Functional Barcode or QR
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .graphicsLayer { rotationY = 180f }
                ) {
                    Text(
                        text = Translations.get(language, "gov_info"),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    IdField(
                        label = Translations.get(language, "passport_number"),
                        value = if (privacyMode) Translations.get(language, "hidden_field") else passportNumber?.ifEmpty { "--" } ?: "--"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    IdField(
                        label = Translations.get(language, "license_number"),
                        value = if (privacyMode) Translations.get(language, "hidden_field") else licenseNumber?.ifEmpty { "--" } ?: "--"
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Decorative barcode for the back
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun IdField(label: String, value: String, isMonospace: Boolean = false, textColor: Color = Color.White) {
    Column {
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
