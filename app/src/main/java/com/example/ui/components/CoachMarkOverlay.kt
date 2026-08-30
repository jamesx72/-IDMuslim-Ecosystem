package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.utils.HapticHelper
import kotlin.math.PI
import kotlin.math.sin

/**
 * Interactive Coach-Mark Overlay for new users upon first app launch.
 * Highlights:
 * 1. How to flip the card (3D interactive flip demonstration).
 * 2. How to access & use NFC verification.
 * 3. How to use the map features & Islamic services (Mosques, Halal spots, Qibla compass).
 */
@Composable
fun CoachMarkOverlay(
    language: String = "fr",
    userName: String = "Membre IDMuslim",
    memberId: String = "IDM-786-2026",
    onNavigateToQibla: (() -> Unit)? = null,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val totalSteps = 3

    Dialog(
        onDismissRequest = { /* Force explicit user completion or skip */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Step Progress & Skip button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Step
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guide interactif ${currentStep + 1}/$totalSteps",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Skip button
                        TextButton(
                            onClick = {
                                HapticHelper.performClick(context, haptic)
                                onComplete()
                            }
                        ) {
                            Text(
                                text = "Passer",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Content Animated Transition
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        label = "coachmark_step_animation"
                    ) { step ->
                        when (step) {
                            0 -> CoachMarkCardFlipStep(
                                userName = userName,
                                memberId = memberId
                            )
                            1 -> CoachMarkNfcStep()
                            2 -> CoachMarkMapStep(
                                onNavigateToQibla = onNavigateToQibla,
                                onDismissCoachMark = onComplete
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Navigation & Dots
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Step Dots
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            repeat(totalSteps) { index ->
                                val isSelected = currentStep == index
                                val width by animateDpAsState(
                                    targetValue = if (isSelected) 28.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = 0.7f),
                                    label = "dot_width"
                                )
                                val color by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    label = "dot_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(8.dp)
                                        .width(width)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color)
                                )
                            }
                        }

                        // Navigation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentStep > 0) {
                                OutlinedButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        currentStep--
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Précédent", fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = {
                                    HapticHelper.performClick(context, haptic)
                                    if (currentStep < totalSteps - 1) {
                                        currentStep++
                                    } else {
                                        HapticHelper.performSuccess(context, haptic)
                                        onComplete()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = if (currentStep < totalSteps - 1) "Suivant" else "Commencer l'expérience",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (currentStep < totalSteps - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * STEP 1: Interactive 3D Card Flip Demonstration
 */
@Composable
private fun CoachMarkCardFlipStep(
    userName: String,
    memberId: String
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 650,
            easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
        ),
        label = "mini_card_flip"
    )

    val flipAngleFraction = (rotation % 180f) / 180f
    val depthScale = 1f - (sin(flipAngleFraction * PI.toFloat()) * 0.08f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_gesture")
    val handTranslate by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hand_movement"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title & description
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "1. Retourner la Carte Numérique",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Touchez la carte d'identité pour basculer en 3D entre le Recto et le Verso sécurisé.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Interactive 3D Card Simulator Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(190.dp),
            contentAlignment = Alignment.Center
        ) {
            // Interactive 3D Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(175.dp)
                    .scale(depthScale)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 16f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        HapticHelper.performClick(context, haptic)
                        isFlipped = !isFlipped
                    },
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                if (rotation <= 90f) {
                    // Front of Card (Recto)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0F172A))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "IDMUSLIM DIGITAL ID",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "RECTO",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.3f))
                                        .border(1.5.dp, Color(0xFF10B981), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = userName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Matricule : $memberId",
                                        color = Color(0xFF93C5FD),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Statut : Vérifié Émeraude",
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Touchez pour voir le verso",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Back of Card (Verso)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF064E3B))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VERSO SÉCURISÉ",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                                ) {
                                    Text(
                                        text = "VERSO",
                                        color = Color(0xFFBAE6FD),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // QR code dummy preview
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Signature SHA-256 Validée",
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Puce NFC ISO/IEC 14443-A",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "Code-barres crypté actif",
                                        color = Color(0xFF93C5FD),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Touchez pour revenir au recto",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Animated Tap Hint Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp + handTranslate.dp, y = (10).dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                HapticHelper.performClick(context, haptic)
                                isFlipped = !isFlipped
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFlipped) "Voir Recto" else "Tester le retournement",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Feature Highlights bullets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            CoachMarkBulletItem(
                icon = Icons.Default.ScreenRotation,
                title = "Geste Intuitif",
                description = "Glissez ou appuyez sur votre carte pour afficher son verso cryptographique."
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoachMarkBulletItem(
                icon = Icons.Default.QrCode2,
                title = "QR Code Dynamique",
                description = "Le QR code au verso permet le contrôle instantané par les autorités et mosquées."
            )
        }
    }
}

/**
 * STEP 2: NFC Contactless Verification Demonstration
 */
@Composable
private fun CoachMarkNfcStep() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isNfcSimulated by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "nfc_waves")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title & description
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "2. Vérification NFC Sans-Contact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Vérifiez et partagez votre statut d'identité sans contact en approchant simplement votre appareil.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // NFC Visual Radar Pulse Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Wave concentric ripples
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(waveScale1)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = waveAlpha1))
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNfcSimulated) Icons.Default.CheckCircle else Icons.Default.Nfc,
                    contentDescription = null,
                    tint = if (isNfcSimulated) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        // Test Interactive NFC Button
        Button(
            onClick = {
                HapticHelper.performScanSuccess(context, haptic)
                isNfcSimulated = true
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isNfcSimulated) Color(0xFF059669) else MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp)
        ) {
            Icon(
                imageVector = if (isNfcSimulated) Icons.Default.Check else Icons.Default.Sensors,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isNfcSimulated) "Signal NFC Détecté & Validé !" else "Simuler un scan NFC sans contact",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Feature Highlights bullets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            CoachMarkBulletItem(
                icon = Icons.Default.Contactless,
                title = "Contrôle à l'Entrée",
                description = "Présentez le dos de votre smartphone aux bornes de mosquées ou d'événements."
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoachMarkBulletItem(
                icon = Icons.Default.QrCodeScanner,
                title = "Lecteur NFC Intégré",
                description = "Retrouvez également le lecteur NFC dédié dans l'onglet 'Scanner'."
            )
        }
    }
}

/**
 * STEP 3: Interactive Map & Islamic Services Demonstration
 */
@Composable
private fun CoachMarkMapStep(
    onNavigateToQibla: (() -> Unit)?,
    onDismissCoachMark: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title & description
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "3. Carte, Mosquées & Qibla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Localisez les mosquées, commerces halal, rassemblements et la direction de la Qibla en temps réel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Map & Radar Preview Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Background grid pattern
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 30.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }
                }

                // Sample Pins
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapPinBadge(
                        title = "Grande Mosquée",
                        subtitle = "650 m • Prières",
                        icon = Icons.Default.Mosque,
                        accentColor = Color(0xFF10B981)
                    )
                    MapPinBadge(
                        title = "Qibla Mecca",
                        subtitle = "118.5° Est • Boussole",
                        icon = Icons.Default.Explore,
                        accentColor = Color(0xFFF59E0B)
                    )
                }
            }
        }

        // Quick Action: Try Qibla Compass
        if (onNavigateToQibla != null) {
            OutlinedButton(
                onClick = {
                    HapticHelper.performClick(context, haptic)
                    onDismissCoachMark()
                    onNavigateToQibla()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ouvrir la Boussole Qibla maintenant",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Feature Highlights bullets
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            CoachMarkBulletItem(
                icon = Icons.Default.LocationOn,
                title = "Onglet 'Communauté'",
                description = "Sélectionnez l'onglet Communauté sur votre profil pour afficher la carte interactive."
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoachMarkBulletItem(
                icon = Icons.Default.Explore,
                title = "Boussole Qibla Précise",
                description = "Accédez au calcul d'azimut en temps réel avec retour haptique d'alignement."
            )
        }
    }
}

@Composable
private fun MapPinBadge(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun CoachMarkBulletItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
