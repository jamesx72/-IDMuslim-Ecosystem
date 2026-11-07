package com.example.ui.components

import android.content.Context
import android.nfc.NfcAdapter
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppDatabase
import com.example.nfc.ProfileApduService
import com.example.utils.HapticHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcHostCardEmulationDialog(
    memberId: String,
    fullName: String,
    verificationStatus: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String,
    nfcChipUid: String,
    cryptoHash: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val isNfcSupported = nfcAdapter != null
    val isNfcEnabled = nfcAdapter?.isEnabled == true

    var isEmulating by remember { mutableStateOf(true) }
    var tapCounter by remember { mutableStateOf(0) }
    var lastSimulatedTapTime by remember { mutableStateOf<Long?>(null) }
    var showPayloadInspector by remember { mutableStateOf(false) }

    // Pulsing NFC wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "nfcPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nfcPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nfcPulseAlpha"
    )

    val nfcPayload = remember(memberId, fullName, verificationStatus, dateOfBirth, residency, communityAffiliation, nfcChipUid, cryptoHash) {
        """
        {
          "header": "IDMUSLIM_HCE_SMART_CARD",
          "aid": "F0010203040506",
          "protocol": "ISO/IEC 7816-4 & 14443-A",
          "memberId": "$memberId",
          "fullName": "${fullName.ifBlank { "Membre IDMuslim" }}",
          "verificationStatus": "${verificationStatus.ifBlank { "VERIFIED" }}",
          "dateOfBirth": "$dateOfBirth",
          "residency": "$residency",
          "community": "$communityAffiliation",
          "chipUid": "$nfcChipUid",
          "sha256Seal": "$cryptoHash",
          "timestamp": ${System.currentTimeMillis() / 1000}
        }
        """.trimIndent()
    }

    LaunchedEffect(nfcPayload) {
        ProfileApduService.activePayload = nfcPayload
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = "HCE Contactless",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NFC Host Card Emulation",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Émulation de Carte Sans-Contact ISO 14443",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // NFC Hardware Status Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            !isNfcSupported -> Color(0xFFFEF2F2)
                            !isNfcEnabled -> Color(0xFFFFFBEB)
                            else -> Color(0xFFECFDF5)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                !isNfcSupported -> Color(0xFFEF4444)
                                !isNfcEnabled -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    !isNfcSupported -> Icons.Default.Cancel
                                    !isNfcEnabled -> Icons.Default.Info
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when {
                                    !isNfcSupported -> Color(0xFFDC2626)
                                    !isNfcEnabled -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when {
                                        !isNfcSupported -> "Matériel NFC non supporté sur ce terminal"
                                        !isNfcEnabled -> "NFC désactivé dans les paramètres Android"
                                        else -> "Service HCE En Ligne & Actif (AID: F0010203040506)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        !isNfcSupported -> Color(0xFF991B1B)
                                        !isNfcEnabled -> Color(0xFF92400E)
                                        else -> Color(0xFF065F46)
                                    }
                                )
                                Text(
                                    text = when {
                                        !isNfcSupported -> "Mode émulation logicielle active pour tests et simulation."
                                        !isNfcEnabled -> "Activez le NFC dans vos paramètres pour autoriser la lecture directe."
                                        else -> "Approchez votre téléphone d'une borne ou d'un autre lecteur NFC."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        !isNfcSupported -> Color(0xFFB91C1C)
                                        !isNfcEnabled -> Color(0xFFB45309)
                                        else -> Color(0xFF047857)
                                    }
                                )
                            }
                        }
                    }

                    // Interactive Pulsing NFC Visual Target
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            // Pulsing radar ripples
                            if (isEmulating) {
                                drawCircle(
                                    color = Color(0xFF0284C7).copy(alpha = pulseAlpha * 0.4f),
                                    radius = (size.minDimension / 2f) * pulseScale,
                                    center = center
                                )
                                drawCircle(
                                    color = Color(0xFF0284C7).copy(alpha = (pulseAlpha * 0.6f)),
                                    radius = (size.minDimension / 2f) * ((pulseScale + 0.5f) / 1.5f),
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

                            // Base Glowing Sphere
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
                                    center = center,
                                    radius = size.minDimension / 2.6f
                                ),
                                radius = size.minDimension / 2.6f,
                                center = center
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contactless,
                                contentDescription = "Tap Reader",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "APPROCHEZ DU LECTEUR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Prêt au contact",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = Color(0xFFE0F2FE)
                            )
                        }
                    }

                    // NFC Emulation Live Specs Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Paramètres de la Carte Émulée",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { showPayloadInspector = !showPayloadInspector }) {
                                    Text(if (showPayloadInspector) "Masquer APDU" else "Inspecter APDU")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("APPLICATION ID (AID)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("F0 01 02 03 04 05 06", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PUCE VIRTUELLE UID", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(nfcChipUid, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("NORME SANS-CONTACT", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("ISO/IEC 7816-4 APDU", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("SCELLÉ CRYPTOGRAPHIQUE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("SHA-256 Authentifié", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }

                            if (showPayloadInspector) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Trame APDU JSON transmise :",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(nfcPayload))
                                                    Toast.makeText(context, "Payload APDU copié !", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copier",
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = nfcPayload,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.5.sp
                                            ),
                                            color = Color(0xFF67E8F9)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Test Terminal Simulation Tool
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simulateur de Borne / Contrôleur d'Accès",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Permet de tester instantanément la réponse APDU et d'enregistrer l'événement dans le journal d'activité sans lecteur physique.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    HapticHelper.performAuthSuccess(context)
                                    tapCounter++
                                    lastSimulatedTapTime = System.currentTimeMillis()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        val dao = AppDatabase.getDatabase(context).activityLogDao()
                                        dao.insertLog(
                                            com.example.data.ActivityLogEntity(
                                                timestamp = System.currentTimeMillis(),
                                                actionType = "NFC_VERIFIED",
                                                description = "Validation sans-contact HCE ($fullName - $memberId)",
                                                location = "Borne d'Entrée Virtuelle HCE"
                                            )
                                        )
                                    }
                                    Toast.makeText(context, "Bip ! Carte HCE validée avec succès.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.TapAndPlay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simuler un Tap NFC (Lecteur Externe)")
                            }

                            if (lastSimulatedTapTime != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Dernier scan réussi ($tapCounter) : Code 0x9000 SW_OK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Fermer le mode Sans-Contact")
                }
            }
        }
    }
}
