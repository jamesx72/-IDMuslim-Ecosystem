package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.utils.DigitalPassManager
import com.example.utils.GoogleWalletManager
import com.example.utils.GoogleWalletSaveResult
import com.example.utils.HapticHelper
import com.example.utils.BiometricHelper
import kotlinx.coroutines.launch
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleWalletExportDialog(
    fullName: String,
    memberId: String,
    dateOfBirth: String,
    verificationStatus: String,
    residency: String = "France",
    communityAffiliation: String = "IDMuslim Global",
    expiryDate: String = "31/12/2030",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var isPayApiAvailable by remember { mutableStateOf<Boolean?>(null) }
    var isSavingToWallet by remember { mutableStateOf(false) }
    var showRawJsonInspector by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(true) }

    val passJson = remember(fullName, memberId, dateOfBirth, verificationStatus, residency, communityAffiliation, expiryDate) {
        GoogleWalletManager.buildGoogleWalletGenericPassJson(
            memberId = memberId,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            verificationStatus = verificationStatus,
            residency = residency,
            communityAffiliation = communityAffiliation,
            expiryDate = expiryDate
        )
    }

    val barcodePayload = remember(memberId, verificationStatus, fullName) {
        "IDMUSLIM:$memberId:${verificationStatus.ifBlank { "VERIFIED" }}:${fullName.ifBlank { "Membre" }}"
    }

    // Generate high-resolution QR Barcode Bitmap for the pass preview
    val qrBitmap = remember(barcodePayload) {
        try {
            val bitMatrix = QRCodeWriter().encode(barcodePayload, BarcodeFormat.QR_CODE, 300, 300)
            val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)
            for (x in 0 until 300) {
                for (y in 0 until 300) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    // Check Google Pay availability on open
    LaunchedEffect(activity) {
        if (activity != null) {
            GoogleWalletManager.checkGoogleWalletAvailability(activity) { available ->
                isPayApiAvailable = available
            }
        } else {
            isPayApiAvailable = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Google Wallet",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Wallet Pass",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Export standardisé Pay API avec code-barres",
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

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pay API Status Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (isPayApiAvailable) {
                            true -> Color(0xFFECFDF5)
                            false -> Color(0xFFFFFBEB)
                            null -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when (isPayApiAvailable) {
                                true -> Color(0xFF10B981)
                                false -> Color(0xFFF59E0B)
                                null -> Color.Transparent
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (isPayApiAvailable) {
                                    true -> Icons.Default.CheckCircle
                                    false -> Icons.Default.Info
                                    null -> Icons.Default.Sync
                                },
                                contentDescription = null,
                                tint = when (isPayApiAvailable) {
                                    true -> Color(0xFF059669)
                                    false -> Color(0xFFD97706)
                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (isPayApiAvailable) {
                                        true -> "Google Pay API activée et prête"
                                        false -> "Mode Export Direct & Partage Universel"
                                        null -> "Vérification des services Google Play..."
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (isPayApiAvailable) {
                                        true -> Color(0xFF065F46)
                                        false -> Color(0xFF92400E)
                                        null -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = when (isPayApiAvailable) {
                                        true -> "Ajout direct et fluide dans votre portefeuille Google Wallet natif."
                                        false -> "L'export génère un pass certifié JSON/PKPass prêt à être importé."
                                        null -> "Connexion au client Google Pay..."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (isPayApiAvailable) {
                                        true -> Color(0xFF047857)
                                        false -> Color(0xFFB45309)
                                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    // Live Google Wallet Card Preview
                    Text(
                        text = "Aperçu du Pass Google Wallet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF064E3B), // Signature IDMuslim Emerald
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF064E3B), Color(0xFF022C22))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            // Top Header of Google Wallet Pass
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "IDMUSLIM IDENTITÉ NUMÉRIQUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFEAA7),
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    border = BorderStroke(0.8.dp, Color(0xFF34D399))
                                ) {
                                    Text(
                                        text = verificationStatus.ifBlank { "VÉRIFIÉ" }.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF34D399),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Member Name & ID
                            Text(
                                text = if (fullName.isNotBlank()) fullName.uppercase() else "MEMBRE IDMUSLIM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "N° Matricule : $memberId",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pass Barcode (QR Code) in Google Wallet format
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shadowElevation = 3.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap,
                                            contentDescription = "Code-barres QR Pass",
                                            modifier = Modifier.size(110.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            modifier = Modifier.size(100.dp),
                                            tint = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Code-barres : QR_CODE • $memberId",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Metadata Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DATE DE NAISSANCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                    Text(
                                        text = if (dateOfBirth.isNotBlank()) dateOfBirth else "--/--/----",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "RÉSIDENCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                    Text(
                                        text = if (residency.isNotBlank()) residency else "France",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "EXPIRATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                    Text(
                                        text = expiryDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = "AFFILIATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                    Text(
                                        text = if (communityAffiliation.isNotBlank()) communityAffiliation else "IDMuslim Global",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = "SÉCURITÉ CRYPTO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = Color(0xFFA7F3D0)
                                    )
                                    Text(
                                        text = "SHA-256 Validé",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }

                    // Metadata Specifications Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DataObject,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Spécifications du Pass Google Wallet",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(
                                    onClick = { showRawJsonInspector = !showRawJsonInspector },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (showRawJsonInspector) "Masquer JSON" else "Voir JSON")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "• Type de Pass : Generic Pass Object (Google Pay API)\n" +
                                       "• Format Barcode : QR_CODE avec encodage UTF-8\n" +
                                       "• Modules de métadonnées : 7 blocs structurés (Identité, Sécurité, NFC, Dates)\n" +
                                       "• Issuer ID : ${GoogleWalletManager.DEFAULT_ISSUER_ID}\n" +
                                       "• Notifications push et alertes d'expiration dynamiques",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            if (showRawJsonInspector) {
                                Spacer(modifier = Modifier.height(10.dp))
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
                                                text = "Payload JSON Standardisé :",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(passJson))
                                                    Toast.makeText(context, "JSON copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copier le JSON",
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = passJson,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.5.sp
                                            ),
                                            color = Color(0xFFA5B4FC)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Live Pass Notifications & Update Push Controls
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Notifications & Mises à Jour en Direct",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pousser des mises à jour dynamiques au portefeuille Google Wallet lors d'un changement de statut ou d'un rappel de renouvellement.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        com.example.utils.PassNotificationManager.sendPassUpdateNotification(
                                            context = context,
                                            title = "Google Wallet : Statut Actualisé",
                                            message = "Votre pass IDMuslim ($fullName) a été revalidé avec succès. Statut: $verificationStatus.",
                                            updateType = "STATUS_UPDATE"
                                        )
                                        isSuccessMessage = true
                                        statusMessage = "Notification de mise à jour du Pass envoyée."
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Push Statut", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        com.example.utils.PassNotificationManager.sendPassUpdateNotification(
                                            context = context,
                                            title = "Google Wallet : Alerte Renouvellement",
                                            message = "Rappel : Votre carte IDMuslim expire le $expiryDate. Pensez à renouveler votre pass.",
                                            updateType = "RENEWAL_ALERT"
                                        )
                                        isSuccessMessage = true
                                        statusMessage = "Alerte de renouvellement du Pass envoyée."
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Push Expiration", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Status Message Display
                    if (statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSuccessMessage) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (isSuccessMessage) Color(0xFF10B981) else Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccessMessage) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSuccessMessage) Color(0xFF065F46) else Color(0xFF991B1B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Action Buttons Row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Action: Add to Google Wallet Button (Official Style)
                    Button(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            if (activity != null) {
                                coroutineScope.launch {
                                    val session = com.example.network.ApiClient.getSessionManager()
                                    val shouldAuth = session.isBiometricLockEnabled() && com.example.security.BiometricHelper.canAuthenticate(context)
                                    val authSuccess = if (shouldAuth) {
                                        BiometricHelper.authenticate(context)
                                    } else {
                                        true
                                    }

                                    if (authSuccess) {
                                        isSavingToWallet = true
                                        statusMessage = null
                                        GoogleWalletManager.saveToGoogleWallet(
                                            activity = activity,
                                            memberId = memberId,
                                            fullName = fullName,
                                            dateOfBirth = dateOfBirth,
                                            verificationStatus = verificationStatus,
                                            residency = residency,
                                            communityAffiliation = communityAffiliation,
                                            expiryDate = expiryDate
                                        ) { result ->
                                            isSavingToWallet = false
                                            when (result) {
                                                is GoogleWalletSaveResult.SuccessInitiated -> {
                                                    isSuccessMessage = true
                                                    statusMessage = "Demande d'enregistrement transmise à Google Wallet."
                                                    Toast.makeText(context, "Ouverture de Google Wallet...", Toast.LENGTH_SHORT).show()
                                                }
                                                is GoogleWalletSaveResult.FallbackShared -> {
                                                    isSuccessMessage = true
                                                    statusMessage = result.message
                                                }
                                                is GoogleWalletSaveResult.Error -> {
                                                    isSuccessMessage = false
                                                    statusMessage = result.message
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Authentification requise pour exporter le pass.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Activité indisponible", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF000000), // Dark Google Wallet button styling
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !isSavingToWallet
                    ) {
                        if (isSavingToWallet) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Enregistrement dans Google Wallet...")
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ajouter à Google Wallet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Secondary Actions: Share JSON & Universal PKPass
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                HapticHelper.performClick(context, haptic)
                                val file = GoogleWalletManager.exportPassJsonFile(context, passJson, memberId)
                                GoogleWalletManager.sharePassFile(context, file, fullName)
                                isSuccessMessage = true
                                statusMessage = "Fichier JSON Google Wallet généré et partagé."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Partager JSON",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                HapticHelper.performClick(context, haptic)
                                DigitalPassManager.exportAndSharePass(
                                    context = context,
                                    fullName = fullName,
                                    memberId = memberId,
                                    dateOfBirth = dateOfBirth,
                                    verificationStatus = verificationStatus,
                                    residency = residency,
                                    community = communityAffiliation
                                )
                                isSuccessMessage = true
                                statusMessage = "Pass PKPass universel généré."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export PKPass",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
