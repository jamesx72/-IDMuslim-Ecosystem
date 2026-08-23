package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A subtle, dynamic holographic digital watermark overlay designed to prevent unauthorized
 * digital copying, cloning, or static screenshots of the IDMuslim credential.
 * 
 * Features:
 * - Dynamic animated rainbow prismatic light sweep (iridescent diffraction grating effect).
 * - Multi-axis shifting spectral interference fringes (Cyan, Emerald, Violet, Gold, Magenta).
 * - Micro-security geometric Guilloché & Rub el Hizb (8-pointed Islamic star) holographic seal.
 * - Dynamic security micro-grid with live anti-counterfeit hash lines.
 */
@Composable
fun HolographicWatermarkOverlay(
    memberId: String = "",
    isVerified: Boolean = true,
    isSuspended: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_transition")

    // Dynamic light sweep animation across card diagonal
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_progress"
    )

    // Subtle continuous rotation for holographic diffraction facets
    val holoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holo_rotation"
    )

    // Gentle breathing pulse for security watermark intensity
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Secondary iridescent wave
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            // 1. Dynamic Spectral / Holographic Prismatic Beam Sweep
            val sweepX = sweepProgress * (width + height)
            val beamWidth = width * 0.75f

            val iridescentColors = if (isSuspended) {
                listOf(
                    Color.Transparent,
                    Color(0xFFFF4D4D).copy(alpha = pulseAlpha * 0.7f),
                    Color(0xFFFF8080).copy(alpha = pulseAlpha * 1.2f),
                    Color(0xFFFFA07A).copy(alpha = pulseAlpha * 0.9f),
                    Color(0xFFFF3333).copy(alpha = pulseAlpha * 0.6f),
                    Color.Transparent
                )
            } else {
                listOf(
                    Color.Transparent,
                    Color(0xFF00FFFF).copy(alpha = pulseAlpha * 0.8f),  // Cyan
                    Color(0xFF34D399).copy(alpha = pulseAlpha * 1.0f),  // Emerald
                    Color(0xFFFDE047).copy(alpha = pulseAlpha * 1.2f),  // Gold
                    Color(0xFFE879F9).copy(alpha = pulseAlpha * 0.9f),  // Orchid / Magenta
                    Color(0xFF60A5FA).copy(alpha = pulseAlpha * 0.7f),  // Sky Blue
                    Color.Transparent
                )
            }

            drawRect(
                brush = Brush.linearGradient(
                    colors = iridescentColors,
                    start = Offset(sweepX - beamWidth, 0f),
                    end = Offset(sweepX, height)
                ),
                blendMode = BlendMode.Screen
            )

            // 2. Holographic Guilloché / Islamic Geometric Star Watermark (Rub el Hizb)
            val centerX = width * 0.76f
            val centerY = height * 0.52f
            val sealRadius = width * 0.18f

            drawHolographicSecuritySeal(
                center = Offset(centerX, centerY),
                radius = sealRadius,
                rotationDegrees = holoRotation,
                alpha = pulseAlpha * 1.1f,
                waveOffset = waveOffset,
                isSuspended = isSuspended
            )

            // 3. Subtle Anti-Counterfeit Micro-Security Guilloché Wave Lines
            drawMicroSecurityWaves(
                width = width,
                height = height,
                waveOffset = waveOffset,
                alpha = pulseAlpha * 0.5f,
                isSuspended = isSuspended
            )

            // 4. Subtle Shimmering Security Watermark Badge (Anti-Screenshot Authenticity Badge)
            drawSecurityMicroGrid(
                width = width,
                height = height,
                alpha = pulseAlpha * 0.35f,
                isSuspended = isSuspended
            )
        }
    }
}

/**
 * Draws the high-security holographic geometric rosette (Rub el Hizb star + concentric interference rings).
 */
private fun DrawScope.drawHolographicSecuritySeal(
    center: Offset,
    radius: Float,
    rotationDegrees: Float,
    alpha: Float,
    waveOffset: Float,
    isSuspended: Boolean
) {
    val primaryHolo = if (isSuspended) Color(0xFFFF5252) else Color(0xFF34D399)
    val secondaryHolo = if (isSuspended) Color(0xFFFFB74D) else Color(0xFF38BDF8)
    val goldHolo = if (isSuspended) Color(0xFFFF8A80) else Color(0xFFFBBF24)

    // Outer concentric interference rings
    for (i in 1..3) {
        val ringRadius = radius * (0.6f + i * 0.2f)
        val ringColor = when (i) {
            1 -> primaryHolo.copy(alpha = alpha * 0.6f)
            2 -> secondaryHolo.copy(alpha = alpha * 0.5f)
            else -> goldHolo.copy(alpha = alpha * 0.4f)
        }
        drawCircle(
            color = ringColor,
            radius = ringRadius,
            center = center,
            style = Stroke(
                width = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), waveOffset * 4f * i)
            )
        )
    }

    // Rotating 8-pointed Star (Rub el Hizb)
    rotate(degrees = rotationDegrees, pivot = center) {
        val squareHalfSide = radius * 0.65f
        
        // Square 1
        drawRect(
            color = goldHolo.copy(alpha = alpha * 0.9f),
            topLeft = Offset(center.x - squareHalfSide, center.y - squareHalfSide),
            size = Size(squareHalfSide * 2, squareHalfSide * 2),
            style = Stroke(width = 1.4f)
        )

        // Square 2 (rotated 45 degrees)
        rotate(degrees = 45f, pivot = center) {
            drawRect(
                color = primaryHolo.copy(alpha = alpha * 0.9f),
                topLeft = Offset(center.x - squareHalfSide, center.y - squareHalfSide),
                size = Size(squareHalfSide * 2, squareHalfSide * 2),
                style = Stroke(width = 1.4f)
            )
        }

        // Inner security emblem circle
        drawCircle(
            color = secondaryHolo.copy(alpha = alpha * 0.8f),
            radius = radius * 0.35f,
            center = center,
            style = Stroke(width = 1.5f)
        )

        // Center dot
        drawCircle(
            color = goldHolo.copy(alpha = alpha * 1.3f),
            radius = 3.5f,
            center = center
        )
    }
}

/**
 * Draws fine anti-tamper sinusoidal wave lines across the card canvas.
 */
private fun DrawScope.drawMicroSecurityWaves(
    width: Float,
    height: Float,
    waveOffset: Float,
    alpha: Float,
    isSuspended: Boolean
) {
    val waveColor = if (isSuspended) Color(0xFFFF6B6B).copy(alpha = alpha) else Color(0xFF6EE7B7).copy(alpha = alpha)
    val waveStep = 32f
    val waveCount = (height / waveStep).toInt()

    for (i in 0..waveCount) {
        val yBase = i * waveStep
        val path = Path()
        var isFirst = true

        val pointsCount = 18
        for (j in 0..pointsCount) {
            val x = (j.toFloat() / pointsCount) * width
            val y = yBase + sin((x / width * 4 * PI.toFloat()) + waveOffset + (i * 0.3f)) * 4.5f
            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = waveColor,
            style = Stroke(width = 0.8f)
        )
    }
}

/**
 * Draws a subtle micro-security dot matrix and angled watermark lines.
 */
private fun DrawScope.drawSecurityMicroGrid(
    width: Float,
    height: Float,
    alpha: Float,
    isSuspended: Boolean
) {
    val dotColor = if (isSuspended) Color(0xFFFF5252).copy(alpha = alpha) else Color(0xFF38BDF8).copy(alpha = alpha)
    
    // Angled fine security hatch lines in top left
    val hatchCount = 6
    for (i in 0 until hatchCount) {
        val offset = i * 18f
        drawLine(
            color = dotColor,
            start = Offset(10f + offset, 10f),
            end = Offset(10f, 10f + offset),
            strokeWidth = 1f
        )
    }

    // Micro security dots along bottom border
    val dots = 12
    val startX = width * 0.08f
    val endX = width * 0.92f
    val step = (endX - startX) / (dots - 1)
    val yPos = height - 12f

    for (d in 0 until dots) {
        drawCircle(
            color = dotColor,
            radius = 1.2f,
            center = Offset(startX + d * step, yPos)
        )
    }
}
