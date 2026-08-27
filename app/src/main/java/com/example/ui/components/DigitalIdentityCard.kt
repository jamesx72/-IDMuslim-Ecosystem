package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun DigitalIdentityCard(
    name: String,
    isVerified: Boolean,
    avatarUrl: String?,
    userId: String,
    modifier: Modifier = Modifier
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "flipAnimation"
    )

    val haptic = LocalHapticFeedback.current
    val isBackSide = rotation > 90f
    var hasInitialized by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(isBackSide) {
        if (hasInitialized) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            hasInitialized = true
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    val qrBitmap = remember(userId, name, isVerified, primaryColor) {
        try {
            val portalUrl = com.example.utils.VerificationPortalHelper.getOrCreateActivePortal(
                memberId = userId,
                fullName = name,
                verificationStatus = if (isVerified) "VERIFIED" else "UNVERIFIED"
            ).url
            val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
            val bitMatrix = QRCodeWriter().encode(portalUrl, BarcodeFormat.QR_CODE, 200, 200, hints)
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            for (x in 0 until 200) {
                for (y in 0 until 200) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) primaryColor else android.graphics.Color.WHITE)
                }
            }
            
            // Draw Islamic geometric pattern (Rub el Hizb) overlay
            val canvas = Canvas(bitmap)
            val center = 100f
            val radius = 24f
            
            // Clear center area to ensure pattern and scannability
            val bgPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, radius + 4f, bgPaint)
            
            val paint = Paint().apply {
                color = primaryColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            
            val rectSize = radius * 0.7f
            val path = Path()
            path.addRect(center - rectSize, center - rectSize, center + rectSize, center + rectSize, Path.Direction.CW)
            
            val path2 = Path()
            path2.addRect(center - rectSize, center - rectSize, center + rectSize, center + rectSize, Path.Direction.CW)
            
            canvas.save()
            canvas.drawPath(path, paint)
            canvas.rotate(45f, center, center)
            canvas.drawPath(path2, paint)
            canvas.restore()
            
            canvas.drawCircle(center, center, rectSize * 0.4f, paint)
            
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { flipped = !flipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (rotation <= 90f) {
            // Front Side
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback placeholder
                            Image(
                                imageVector = Icons.Default.AccountCircle, // Placeholder
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                alpha = 0.5f,
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Name and Status
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = if (isVerified) "Verified" else "Unverified",
                                tint = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVerified) "Verified User" else "Unverified Account",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (isVerified) Color(0xFF4CAF50) else Color(0xFFFFC107)
                            )
                        }
                    }

                    if (qrBitmap != null) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "Verification QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                HolographicWatermarkOverlay(
                    memberId = userId,
                    isVerified = isVerified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Back Side
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(24.dp)
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "User Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ID: $userId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Membership: Standard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Expires: 12/2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HolographicWatermarkOverlay(
                    memberId = userId,
                    isVerified = isVerified,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                )
            }
        }
    }
}
