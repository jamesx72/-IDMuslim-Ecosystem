package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.HapticHelper
import com.example.utils.SolarPhase
import com.example.utils.SolarState
import com.example.utils.SolarThemeHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SolarHorizonCard(
    solarState: SolarState,
    isSolarAdaptiveEnabled: Boolean,
    activeOverride: String,
    onToggleAdaptive: (Boolean) -> Unit,
    onSelectOverride: (String) -> Unit,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Animated glow pulse for sun/moon
    val infiniteTransition = rememberInfiniteTransition(label = "solar_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(solarState.adaptedAccentColor.copy(alpha = 0.3f), solarState.adaptedAccentColor.copy(alpha = 0.1f))
                                )
                            )
                            .border(1.dp, solarState.adaptedAccentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = solarState.icon,
                            contentDescription = null,
                            tint = solarState.adaptedAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == "fr") "Thème Solaire Dynamique" else "Solar-Adaptive Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "fr") "Ajustement automatique Lever/Coucher" else "Auto Sunrise/Sunset Tone Adaptation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isSolarAdaptiveEnabled,
                    onCheckedChange = {
                        HapticHelper.performClick(context, haptic)
                        onToggleAdaptive(it)
                    }
                )
            }

            if (isSolarAdaptiveEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Horizon Arch & Sun Position Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    solarState.adaptedGradientColors.first().copy(alpha = 0.85f),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = solarState.cycleProgressFraction,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "solar_arc_progress"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val baselineY = h * 0.76f

                        // Horizon baseline
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(0f, baselineY),
                            end = Offset(w, baselineY),
                            strokeWidth = 2f
                        )

                        // Dotted Arch trajectory
                        val arcRadiusX = w * 0.42f
                        val arcRadiusY = h * 0.62f
                        val centerX = w * 0.5f

                        val path = Path()
                        val steps = 60
                        for (i in 0..steps) {
                            val angle = PI - (i.toFloat() / steps) * PI
                            val x = centerX + arcRadiusX * cos(angle).toFloat()
                            val y = baselineY - arcRadiusY * sin(angle).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.25f),
                            style = Stroke(
                                width = 2.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                            )
                        )

                        // Calculate current celestial body position along the arch
                        val currentAngle = PI - (animatedProgress.coerceIn(0f, 1f) * PI)
                        val sunX = centerX + arcRadiusX * cos(currentAngle).toFloat()
                        val sunY = baselineY - arcRadiusY * sin(currentAngle).toFloat()

                        // Sun/Moon glow halo
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    solarState.adaptedAccentColor.copy(alpha = 0.55f * pulseGlow),
                                    solarState.adaptedAccentColor.copy(alpha = 0.0f)
                                ),
                                center = Offset(sunX, sunY),
                                radius = 34f * pulseGlow
                            ),
                            center = Offset(sunX, sunY),
                            radius = 34f * pulseGlow
                        )

                        // Celestial disc
                        drawCircle(
                            color = solarState.adaptedAccentColor,
                            radius = 7.5f,
                            center = Offset(sunX, sunY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(sunX, sunY)
                        )
                    }

                    // Overlay information labels
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top info badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = solarState.adaptedAccentColor.copy(alpha = 0.22f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, solarState.adaptedAccentColor.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = solarState.icon,
                                        contentDescription = null,
                                        tint = solarState.adaptedAccentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == "fr") solarState.phase.titleFr.uppercase() else solarState.phase.titleEn.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = solarState.adaptedAccentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Text(
                                text = solarState.locationLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        // Bottom Sunrise / Sunset times
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🌅 ${solarState.sunriseTimeStr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = "⏰ ${solarState.currentLocalTimeStr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessMedium,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🌇 ${solarState.sunsetTimeStr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Next solar transition banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = solarState.nextTransitionSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Simulator / Quick Preview Filter Chips
                Text(
                    text = if (language == "fr") "Simulateur / Aperçu des Phases Solaires :" else "Solar Phase Preview / Simulation :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val simulationChips = listOf(
                    "AUTO" to (if (language == "fr") "Temps Réel" else "Realtime"),
                    "DAY" to (if (language == "fr") "☀️ Plein Jour" else "☀️ Daytime"),
                    "SUNSET" to (if (language == "fr") "🌅 Soirée / Chaud" else "🌅 Sunset / Warm"),
                    "NIGHT" to (if (language == "fr") "🌙 Nuit" else "🌙 Night"),
                    "DAWN" to (if (language == "fr") "🌄 Aube" else "🌄 Dawn")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    simulationChips.forEach { (key, label) ->
                        val isSelected = activeOverride.equals(key, ignoreCase = true)
                        Surface(
                            onClick = {
                                HapticHelper.performClick(context, haptic)
                                onSelectOverride(key)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
