package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DigitalVerifiedBadge(
    isVerified: Boolean,
    memberId: String,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.SMALL,
    fullName: String = ""
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Pulsing/glow effect for verified state
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val badgeGradient = if (isVerified) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF10B981), Color(0xFF047857)) // Vivid Emerald
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF9CA3AF), Color(0xFF4B5563)) // Neat Slate Gray
        )
    }

    if (size == BadgeSize.SMALL) {
        // Simple, elegant compact circular chip with a pulse
        Surface(
            modifier = modifier
                .testTag("verified_badge_small")
                .clickable { showDetailsDialog = true }
                .padding(2.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isVerified) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF9CA3AF).copy(alpha = 0.15f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isVerified) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF9CA3AF).copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(badgeGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Badge",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVerified) "VÉRIFIÉ" else "NON VÉRIFIÉ",
                    color = if (isVerified) Color(0xFF34D399) else Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    } else {
        // High fidelity full display widget card showing user verification status
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("verified_badge_large")
                .clickable { showDetailsDialog = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(badgeGradient, CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = "Status Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isVerified) "BADGE CITOYEN VÉRIFIÉ" else "VÉRIFICATION INITIALE REQUIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isVerified) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isVerified) "Identité numérique confirmée & scellée" else "Terminez les étapes d'accréditation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            if (isVerified) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFF9CA3AF).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VOIR PROUVÉ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerified) Color(0xFF10B981) else Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }

    // Cryptographic Proof Certificate View Dialog
    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .testTag("verification_details_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Area
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Status",
                        tint = if (isVerified) Color(0xFF10B981) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LE PASSEPORT CITOYEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Certification d'Accréditation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Glowing Signature Star Circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(1.5.dp, if (isVerified) Color(0xFF34D399) else Color(0xFF9CA3AF), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(badgeGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = "Seal icon",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isVerified) "STATUT : CERTIFIÉ CONFORME" else "STATUT : EN ATTENTE DE CONFORMITÉ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerified) Color(0xFF10B981) else Color(0xFFEE2D2D)
                    )
                    
                    if (fullName.isNotBlank()) {
                        Text(
                            text = "Titulaire : ${fullName.uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Checklist details showcasing completed verification steps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "PROTOCOLES DE SÉCURITÉ CONCERNÉS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        VerificationLineItem(
                            stepName = "Authentification de l'adresse e-mail",
                            isCompleted = true // Initial step is completed during registration
                        )
                        VerificationLineItem(
                            stepName = "Paire de clés biométriques uniques",
                            isCompleted = isVerified
                        )
                        VerificationLineItem(
                            stepName = "Vérification administrative des pièces",
                            isCompleted = isVerified
                        )
                        VerificationLineItem(
                            stepName = "Signature d'empreinte NFC chiffrée",
                            isCompleted = isVerified
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cryptographic Metadata String (Fake blockchain/NFC styled hex proof)
                    val cryptoHash = remember(memberId, isVerified) {
                        val base = if (isVerified) "IDMUSLIM-VERIFIED-SECURED-SHA256" else "IDMUSLIM-PENDING"
                        val prefix = String.format("%08x", (memberId + base).hashCode())
                        "PROOF_SHA256: 0x${prefix}4b92b604e76a0d6edbc3fc2ef9aa8f3"
                    }
                    Text(
                        text = cryptoHash,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(
                        onClick = { showDetailsDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fermer la certification", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationLineItem(stepName: String, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stepName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

enum class BadgeSize {
    SMALL,
    LARGE
}
