package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.locales.Translations
import com.example.utils.HapticHelper
import com.example.utils.QRCodeGenerator
import com.example.utils.VerificationPortalHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebVerificationPortalDialog(
    memberId: String,
    fullName: String,
    verificationStatus: String,
    communityAffiliation: String = "IDMuslim Global Community",
    dateOfBirth: String = "",
    residency: String = "",
    photoBase64: String? = null,
    language: String = "fr",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedDuration by remember { mutableStateOf(VerificationPortalHelper.ExpirationPreset.MINUTES_15) }
    var includeDob by remember { mutableStateOf(true) }
    var includeResidency by remember { mutableStateOf(true) }
    var includePhoto by remember { mutableStateOf(true) }
    var includeCommunity by remember { mutableStateOf(true) }

    var showWebPortalSimulation by remember { mutableStateOf(false) }

    // Generate unique portal URL
    var portalData by remember {
        mutableStateOf(
            VerificationPortalHelper.generatePortalUrl(
                memberId = memberId,
                fullName = fullName,
                verificationStatus = verificationStatus,
                community = communityAffiliation,
                dateOfBirth = dateOfBirth,
                residency = residency,
                photoBase64 = photoBase64,
                durationSeconds = selectedDuration.seconds,
                includeDob = includeDob,
                includeResidency = includeResidency,
                includePhoto = includePhoto,
                includeCommunity = includeCommunity
            )
        )
    }

    fun refreshPortalUrl() {
        portalData = VerificationPortalHelper.generatePortalUrl(
            memberId = memberId,
            fullName = fullName,
            verificationStatus = verificationStatus,
            community = communityAffiliation,
            dateOfBirth = dateOfBirth,
            residency = residency,
            photoBase64 = photoBase64,
            durationSeconds = selectedDuration.seconds,
            includeDob = includeDob,
            includeResidency = includeResidency,
            includePhoto = includePhoto,
            includeCommunity = includeCommunity
        )
    }

    // QR bitmap generated from unique URL
    val qrBitmap = remember(portalData.url) {
        QRCodeGenerator.generateQRCode(portalData.url, 480)
    }

    // Live countdown timer
    var remainingSeconds by remember(portalData.expiresAtSeconds) {
        mutableLongStateOf(
            maxOf(0L, portalData.expiresAtSeconds - (System.currentTimeMillis() / 1000))
        )
    }

    LaunchedEffect(portalData.expiresAtSeconds) {
        while (true) {
            val left = portalData.expiresAtSeconds - (System.currentTimeMillis() / 1000)
            remainingSeconds = maxOf(0L, left)
            if (left <= 0) {
                break
            }
            delay(1000L)
        }
    }

    fun formatSeconds(sec: Long): String {
        val m = sec / 60
        val s = sec % 60
        val h = m / 60
        return if (h > 0) {
            "%02d:%02d:%02d".format(h, m % 60, s)
        } else {
            "%02d:%02d".format(m, s)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Translations.get(language, "web_portal_title").ifBlank { "Portail de Vérification Web" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Translations.get(language, "web_portal_subtitle").ifBlank { "Accès public temporaire sans application requise" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explanatory Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Translations.get(language, "web_portal_banner_text").ifBlank {
                                "Ce QR code encode une URL unique et sécurisée. Toute personne ou autorité tierce scannant ce code accédera immédiatement au portail web IDMuslim certifié pour confirmer votre authenticité en temps réel."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code Container with rotating border glow and Countdown pill
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Web Portal QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Expiration & Timer Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (remainingSeconds > 60) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFFEF2F2),
                    border = BorderStroke(
                        1.dp,
                        if (remainingSeconds > 60) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else Color(0xFFEF4444)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (remainingSeconds > 60) MaterialTheme.colorScheme.secondary else Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (remainingSeconds > 0) {
                                "${Translations.get(language, "valid_for").ifBlank { "Valide encore :" }} ${formatSeconds(remainingSeconds)}"
                            } else {
                                Translations.get(language, "expired_token").ifBlank { "Lien expiré • Régénérez un nouveau code" }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds > 60) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFFB91C1C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generated URL Display Box with Copy Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "URL UNIQUE DE VÉRIFICATION :",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = portalData.url,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("IDMuslim Verification Portal", portalData.url)
                                clipboard?.setPrimaryClip(clip)
                                HapticHelper.performClick(context, haptic)
                                Toast.makeText(context, "URL du portail copiée !", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy URL",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: Share & Test Portal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "IDMuslim - Vérification d'Identité Numérique")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Voici mon lien sécurisé de vérification d'identité IDMuslim certifiée : ${portalData.url}\n(Ce lien temporaire permet de vérifier l'authenticité de mon ID sans installer d'application)."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager le lien de vérification"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            showWebPortalSimulation = true
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aperçu Web", fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            refreshPortalUrl()
                            Toast.makeText(context, "Nouveau jeton cryptographique généré !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Token",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Accordion / Duration & Privacy toggles
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DURÉE DE VALIDITÉ DU LIEN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        VerificationPortalHelper.ExpirationPreset.MINUTES_15 to "15 min",
                        VerificationPortalHelper.ExpirationPreset.HOURS_1 to "1h",
                        VerificationPortalHelper.ExpirationPreset.HOURS_4 to "4h",
                        VerificationPortalHelper.ExpirationPreset.HOURS_24 to "24h",
                        VerificationPortalHelper.ExpirationPreset.DAYS_7 to "7j"
                    ).forEach { (preset, label) ->
                        val isSel = selectedDuration == preset
                        Surface(
                            onClick = {
                                selectedDuration = preset
                                refreshPortalUrl()
                                HapticHelper.performClick(context, haptic)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cryptographic Proof Details Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sceau de Sécurité : ${portalData.cryptoHash}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Protocole : Web Portal v2.1 • Signature HMAC-SHA256 • Registre IDMuslim",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // THIRD-PARTY WEB PORTAL SIMULATION VIEWER
    // Shows exactly what a third party sees in their browser when scanning the QR
    // =========================================================================
    if (showWebPortalSimulation) {
        Dialog(
            onDismissRequest = { showWebPortalSimulation = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.94f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1520)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Browser Window Topbar Mockup
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "SSL Encrypted",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "verify.idmuslim.org/portal?id=${memberId}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Public Web Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IDMUSLIM VERIFICATION GATEWAY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Portail Public de Vérification d'Authenticité",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Authenticated Status Banner with Pulsing Ring
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.6f),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "IDENTITÉ AUTHENTIQUE ET VALIDE",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Le certificat numérique associé à cet utilisateur a été émis et validé avec succès par le protocole IDMuslim.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1FAE5),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Session active • Expire dans ${formatSeconds(remainingSeconds)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFDE68A),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Public Profile Details Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Member ID & Level
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "IDENTIFIANT MEMBRE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = memberId.ifBlank { "IDM-7860-9942" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF6EE7B7)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF047857).copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = verificationStatus.ifBlank { "VÉRIFIÉ NIVEAU 3" }.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399),
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155))

                            // Name
                            Column {
                                Text(
                                    text = "NOM & PRÉNOM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = fullName.ifBlank { "Membre Communautaire" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Community affiliation
                            if (includeCommunity && communityAffiliation.isNotBlank()) {
                                Column {
                                    Text(
                                        text = "AFFILIATION COMMUNAUTAIRE / MOSQUÉE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = communityAffiliation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }

                            // DOB & Residency if included
                            if (includeDob && dateOfBirth.isNotBlank()) {
                                Column {
                                    Text(
                                        text = "DATE DE NAISSANCE CERTIFIÉE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = dateOfBirth,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }

                            if (includeResidency && residency.isNotBlank()) {
                                Column {
                                    Text(
                                        text = "RÉSIDENCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = residency,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155))

                            // Security audit hash
                            Column {
                                Text(
                                    text = "SIGNATURE CRYPTOGRAPHIQUE CONTRÔLÉE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 8.5.sp
                                )
                                Text(
                                    text = portalData.cryptoHash,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF93C5FD),
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Public Notice Footer
                    Text(
                        text = "Vérification en temps réel hébergée sur IDMuslim Gateway.\nAucune installation d'application requise par le vérificateur.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        fontSize = 9.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showWebPortalSimulation = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fermer l'Aperçu du Portail", color = Color.White)
                    }
                }
            }
        }
    }
}
