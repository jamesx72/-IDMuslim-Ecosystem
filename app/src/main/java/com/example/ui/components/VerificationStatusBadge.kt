package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerificationStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    substep: String = "",
    useDarkThemeColors: Boolean = false
) {
    // Elegant pulsing animation for pending state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val badgeColor: Color
    val contentColor: Color
    val labelText: String
    val icon = when (status.uppercase()) {
        "VERIFIED" -> {
            badgeColor = if (useDarkThemeColors) Color(0xFFE6F4EA) else Color(0xFF34D399).copy(alpha = 0.2f)
            contentColor = if (useDarkThemeColors) Color(0xFF137333) else Color(0xFF10B981)
            labelText = "Vérifié"
            Icons.Default.CheckCircle
        }
        "PENDING" -> {
            badgeColor = if (useDarkThemeColors) Color(0xFFFEF7E0) else Color(0xFFF59E0B).copy(alpha = 0.2f)
            contentColor = if (useDarkThemeColors) Color(0xFFB06000) else Color(0xFFF59E0B)
            labelText = "Attente d'approbation"
            Icons.Default.Refresh
        }
        else -> {
            badgeColor = if (useDarkThemeColors) Color(0xFFFCE8E6) else Color(0xFFEF4444).copy(alpha = 0.15f)
            contentColor = if (useDarkThemeColors) Color(0xFFC5221F) else Color(0xFFEF4444)
            labelText = "Non vérifié"
            Icons.Default.Warning
        }
    }

    val isPending = status.uppercase() == "PENDING"
    val alphaModifier = if (isPending) Modifier.alpha(pulseAlpha) else Modifier

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = badgeColor,
            contentColor = contentColor,
            modifier = alphaModifier
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = labelText.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = contentColor.copy(alpha = 0.3f),
                            blurRadius = 2f
                        )
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = contentColor
                )
            }
        }
        
        if (isPending && substep.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = substep,
                style = MaterialTheme.typography.bodySmall,
                color = if (useDarkThemeColors) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
