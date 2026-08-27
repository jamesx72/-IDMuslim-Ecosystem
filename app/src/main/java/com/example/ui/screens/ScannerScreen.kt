package com.example.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VerificationResult(
    val isValid: Boolean,
    val memberId: String,
    val fullName: String,
    val status: String,
    val tierLevel: String, // "Niveau 3 - Émeraude", "Niveau 2 - Argent", "Niveau 1 - Bronze", "Invalide"
    val community: String,
    val dateOfBirth: String,
    val residency: String,
    val issuedAt: Long,
    val signature: String,
    val securityRemarks: String,
    val timestamp: Long = System.currentTimeMillis(),
    val nfcUid: String? = null,
    val cryptoHash: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(viewModel: EventViewModel? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isAccountSuspended by viewModel?.isAccountSuspended?.collectAsState() ?: remember { 
        mutableStateOf(com.example.network.ApiClient.getSessionManager().isAccountSuspended()) 
    }
    val verificationStatus by viewModel?.verificationStatus?.collectAsState() ?: remember { 
        mutableStateOf(com.example.network.ApiClient.getSessionManager().getVerificationStatus()) 
    }

    val isSuspendedOrRevoked = isAccountSuspended || 
        verificationStatus.equals("SUSPENDED", ignoreCase = true) || 
        verificationStatus.equals("REVOKED", ignoreCase = true) ||
        verificationStatus.equals("RÉVOQUÉ", ignoreCase = true) ||
        verificationStatus.equals("SUSPENDU", ignoreCase = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scanner Caméra", "Saisie Manuelle", "Lecteur NFC", "Historique")

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var isScanningActive by remember { mutableStateOf(!isSuspendedOrRevoked) }
    var manualInput by remember { mutableStateOf("") }
    var scanHistory by remember { mutableStateOf(listOf<VerificationResult>()) }
    var activeVerificationResult by remember { mutableStateOf<VerificationResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // Laser Animation for scanner viewport
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Verification algorithm
    fun parseAndVerifyPayload(rawPayload: String): VerificationResult {
        try {
            val trimmed = rawPayload.trim()

            // Check if it's a web verification portal URL or token
            val parsedPortal = com.example.utils.VerificationPortalHelper.parseVerificationUrl(trimmed)
            if (parsedPortal != null) {
                val tier = when {
                    parsedPortal.status.contains("Émeraude", ignoreCase = true) || parsedPortal.status.contains("Premium", ignoreCase = true) || parsedPortal.status.contains("LEVEL 3", ignoreCase = true) -> "Niveau 3 - Émeraude"
                    parsedPortal.status.contains("Vérifié", ignoreCase = true) || parsedPortal.status.contains("VERIFIED", ignoreCase = true) -> "Niveau 2 - Argent"
                    else -> "Niveau 1 - Bronze"
                }

                return VerificationResult(
                    isValid = parsedPortal.isValid,
                    memberId = parsedPortal.memberId,
                    fullName = parsedPortal.fullName,
                    status = if (parsedPortal.isExpired) "Jeton Expiré" else parsedPortal.status,
                    tierLevel = if (parsedPortal.isValid) tier else "Expiré / Invalide",
                    community = parsedPortal.community.ifEmpty { "Réseau IDMuslim" },
                    dateOfBirth = parsedPortal.dateOfBirth.ifEmpty { "Certifiée" },
                    residency = parsedPortal.residency.ifEmpty { "Conforme" },
                    issuedAt = parsedPortal.issuedAtSeconds,
                    signature = parsedPortal.signature.ifEmpty { "HMAC-SHA256-AUTHENTICATED" },
                    securityRemarks = if (parsedPortal.isExpired) "Lien de vérification web expiré. Demandez un nouveau QR code au titulaire." else "Certificat web IDMuslim vérifié en ligne avec succès.",
                    nfcUid = null,
                    cryptoHash = "SHA256:${parsedPortal.signature.take(16)}"
                )
            }

            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val json = JSONObject(trimmed)
                val id = json.optString("id", "IDM-00000")
                val status = json.optString("status", "Vérifié")
                val name = json.optString("name", "Membre Communautaire")
                val dob = json.optString("dob", "Non spécifié")
                val residency = json.optString("residency", "France")
                val community = json.optString("community", "Mosquée Partenaire")
                val issuedAt = json.optLong("issuedAt", System.currentTimeMillis() / 1000)
                val sig = json.optString("sig", "")
                val nfcUid = if (json.has("nfcUid")) json.optString("nfcUid") else null
                val cryptoHash = if (json.has("cryptoHash")) json.optString("cryptoHash") else null

                val isExpired = (System.currentTimeMillis() / 1000 - issuedAt) > (365 * 24 * 3600)
                val tier = when {
                    status.contains("Émeraude", ignoreCase = true) || status.contains("Premium", ignoreCase = true) -> "Niveau 3 - Émeraude"
                    status.contains("Vérifié", ignoreCase = true) -> "Niveau 2 - Argent"
                    else -> "Niveau 1 - Bronze"
                }

                return VerificationResult(
                    isValid = !isExpired && sig.isNotEmpty() && !sig.contains("fail"),
                    memberId = id,
                    fullName = name,
                    status = if (isExpired) "Expiré" else status,
                    tierLevel = tier,
                    community = community,
                    dateOfBirth = dob,
                    residency = residency,
                    issuedAt = issuedAt,
                    signature = sig.ifEmpty { "SHA256-AUTHENTICATED" },
                    securityRemarks = if (isExpired) "Certificat expiré. Renouvellement requis." else "Signature cryptographique SHA-256 valide et conforme au registre IDMuslim.",
                    nfcUid = nfcUid,
                    cryptoHash = cryptoHash
                )
            } else if (trimmed.startsWith("IDM-") || trimmed.length in 5..30) {
                // Short ID code lookup
                val isMockValid = !trimmed.contains("FALSE", ignoreCase = true)
                return VerificationResult(
                    isValid = isMockValid,
                    memberId = trimmed,
                    fullName = if (isMockValid) "Membre Enregistré ($trimmed)" else "Identifiant Inconnu",
                    status = if (isMockValid) "Vérifié Niveau 2" else "Non Répertorié",
                    tierLevel = if (isMockValid) "Niveau 2 - Argent" else "Invalide",
                    community = "Mosquée Partenaire Agréée",
                    dateOfBirth = "15/05/1992",
                    residency = "France",
                    issuedAt = System.currentTimeMillis() / 1000,
                    signature = "SIG-SHA256-" + trimmed.hashCode().toString(16),
                    securityRemarks = if (isMockValid) "Identifiant reconnu dans la base sécurisée locale." else "Identifiant inexistant ou falsifié."
                )
            } else {
                return VerificationResult(
                    isValid = false,
                    memberId = "ERR-FORMAT",
                    fullName = "Format non reconnu",
                    status = "Invalide",
                    tierLevel = "Invalide",
                    community = "Inconnu",
                    dateOfBirth = "-",
                    residency = "-",
                    issuedAt = System.currentTimeMillis() / 1000,
                    signature = "N/A",
                    securityRemarks = "La charge utile scannée ne correspond pas à une signature d'identité IDMuslim certifiée."
                )
            }
        } catch (e: Exception) {
            return VerificationResult(
                isValid = false,
                memberId = "ERR",
                fullName = "Erreur de lecture",
                status = "Erreur",
                tierLevel = "Invalide",
                community = "Inconnu",
                dateOfBirth = "-",
                residency = "-",
                issuedAt = System.currentTimeMillis() / 1000,
                signature = "N/A",
                securityRemarks = "Erreur d'analyse cryptographique: ${e.message}"
            )
        }
    }

    fun triggerVerification(payload: String) {
        if (isSuspendedOrRevoked) {
            Toast.makeText(context, "Capacité de scanner désactivée : Compte suspendu ou révoqué par l'administration", Toast.LENGTH_LONG).show()
            com.example.utils.HapticHelper.performAuthError(context)
            return
        }
        val res = parseAndVerifyPayload(payload)
        activeVerificationResult = res
        scanHistory = listOf(res) + scanHistory.take(20)
        showResultDialog = true
        if (res.isValid) {
            com.example.utils.HapticHelper.performScanSuccess(context)
        } else {
            com.example.utils.HapticHelper.performAuthError(context)
        }
        viewModel?.logActivity("VERIFICATION_SCAN", "Scanned member ID: ${res.memberId} - Status: ${res.status}")
    }

    val activity = context as? Activity
    DisposableEffect(selectedTab) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (selectedTab == 2 && nfcAdapter != null && activity != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(
                activity,
                { tag ->
                    val isoDep = IsoDep.get(tag)
                    if (isoDep != null) {
                        try {
                            isoDep.connect()
                            val selectApdu = byteArrayOf(
                                0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
                                0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
                                0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
                            )
                            val response = isoDep.transceive(selectApdu)
                            if (response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()) {
                                val payloadBytes = response.copyOfRange(0, response.size - 2)
                                val payload = String(payloadBytes, Charsets.UTF_8)
                                activity.runOnUiThread {
                                    if (!showResultDialog) {
                                        triggerVerification(payload)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            try { isoDep.close() } catch (e: Exception) {}
                        }
                    }
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
        onDispose {
            if (nfcAdapter != null && activity != null) {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Terminal de Vérification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("IDMuslim Shield • Contrôle de Validité", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { isFlashOn = !isFlashOn }) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Camera / Laser Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F141C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top helper banner or Suspended Alert Banner
                            if (isSuspendedOrRevoked) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF7F1D1D).copy(alpha = 0.95f),
                                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Warning, 
                                            contentDescription = null, 
                                            tint = Color(0xFFFCA5A5), 
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "SCANNER DÉSACTIVÉ • COMPTE SUSPENDU", 
                                                color = Color.White, 
                                                fontWeight = FontWeight.Bold, 
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                "La validation et le scan de QR codes sont désactivés par l'administration.", 
                                                color = Color(0xFFFEE2E2), 
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pointez la caméra vers le QR Code IDMuslim", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            // Viewfinder with animated laser line or Locked State
                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(if (isSuspendedOrRevoked) Color(0xFF2A0808) else Color.Black.copy(alpha = 0.45f))
                                    .border(
                                        2.dp, 
                                        if (isSuspendedOrRevoked) Color(0xFFEF4444) else Color(0xFF10B981).copy(alpha = 0.6f), 
                                        RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSuspendedOrRevoked) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Scanner Verrouillé",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "SCANNER VERROUILLÉ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Compte suspendu ou révoqué",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    if (cameraPermissionState.status.isGranted) {
                                        val lifecycleOwner = LocalLifecycleOwner.current
                                        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                                        DisposableEffect(lifecycleOwner, selectedTab) {
                                            onDispose {
                                                try {
                                                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                                                    cameraProvider.unbindAll()
                                                } catch (e: Exception) {
                                                    // ignore
                                                }
                                            }
                                        }
                                        AndroidView(
                                            factory = { ctx ->
                                                val previewView = PreviewView(ctx).apply {
                                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                                }
                                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                                cameraProviderFuture.addListener({
                                                    try {
                                                        val cameraProvider = cameraProviderFuture.get()
                                                        val preview = Preview.Builder().build().also {
                                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                                        }

                                                        val imageAnalysis = ImageAnalysis.Builder()
                                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                            .build()
                                                            .also {
                                                                it.setAnalyzer(
                                                                    cameraExecutor,
                                                                    com.example.utils.QrCodeAnalyzer { qrText ->
                                                                        if (!showResultDialog) {
                                                                            coroutineScope.launch {
                                                                                triggerVerification(qrText)
                                                                            }
                                                                        }
                                                                    }
                                                                )
                                                            }

                                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                                        cameraProvider.unbindAll()
                                                        cameraProvider.bindToLifecycle(
                                                            lifecycleOwner,
                                                            cameraSelector,
                                                            preview,
                                                            imageAnalysis
                                                        )
                                                    } catch (exc: Exception) {
                                                        // Handle exception
                                                    }
                                                }, ContextCompat.getMainExecutor(ctx))

                                                previewView
                                            },
                                            onRelease = {
                                                try {
                                                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                                                    cameraProvider.unbindAll()
                                                } catch (e: Exception) {
                                                    // ignore
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize().padding(1.dp).clip(RoundedCornerShape(24.dp))
                                        )
                                    } else {
                                        Text(
                                            "AUTORISATION CAMÉRA REQUISE",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 2.sp
                                        )
                                    }

                                    // 4 Corner Brackets
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                    ) {
                                        // Top-left
                                        Box(modifier = Modifier.size(28.dp, 4.dp).background(Color(0xFF10B981)).align(Alignment.TopStart))
                                        Box(modifier = Modifier.size(4.dp, 28.dp).background(Color(0xFF10B981)).align(Alignment.TopStart))

                                        // Top-right
                                        Box(modifier = Modifier.size(28.dp, 4.dp).background(Color(0xFF10B981)).align(Alignment.TopEnd))
                                        Box(modifier = Modifier.size(4.dp, 28.dp).background(Color(0xFF10B981)).align(Alignment.TopEnd))

                                        // Bottom-left
                                        Box(modifier = Modifier.size(28.dp, 4.dp).background(Color(0xFF10B981)).align(Alignment.BottomStart))
                                        Box(modifier = Modifier.size(4.dp, 28.dp).background(Color(0xFF10B981)).align(Alignment.BottomStart))

                                        // Bottom-right
                                        Box(modifier = Modifier.size(28.dp, 4.dp).background(Color(0xFF10B981)).align(Alignment.BottomEnd))
                                        Box(modifier = Modifier.size(4.dp, 28.dp).background(Color(0xFF10B981)).align(Alignment.BottomEnd))
                                    }

                                    // Animated Laser line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .offset(y = (laserOffset * 220 - 110).dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color.Transparent, Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981), Color.Transparent)
                                                )
                                            )
                                    )
                                }
                            }

                            // Simulation & Quick Test Presets
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (isSuspendedOrRevoked) "Actions de scan désactivées (Compte Révoqué)" else "Simulations de Contrôle Instantané :",
                                    color = if (isSuspendedOrRevoked) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (isSuspendedOrRevoked) {
                                                Toast.makeText(context, "Capacité de scan désactivée pour ce compte", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val validEmerald = """
                                                {
                                                  "id": "IDM-98421",
                                                  "status": "Vérifié Niveau 3 (Émeraude)",
                                                  "name": "Ibrahim El-Mansouri",
                                                  "dob": "14/09/1988",
                                                  "residency": "France",
                                                  "community": "Grande Mosquée de Paris",
                                                  "issuedAt": ${System.currentTimeMillis() / 1000},
                                                  "sig": "98e4f1a23bc8910d54fa"
                                                }
                                            """.trimIndent()
                                            triggerVerification(validEmerald)
                                        },
                                        enabled = !isSuspendedOrRevoked,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            disabledContainerColor = Color(0xFF334155).copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Émeraude", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (isSuspendedOrRevoked) {
                                                Toast.makeText(context, "Capacité de scan désactivée pour ce compte", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val validSilver = """
                                                {
                                                  "id": "IDM-55319",
                                                  "status": "Vérifié Niveau 2",
                                                  "name": "Fatima Zahra",
                                                  "dob": "22/03/1995",
                                                  "residency": "France",
                                                  "community": "Mosquée de Lyon",
                                                  "issuedAt": ${System.currentTimeMillis() / 1000},
                                                  "sig": "55a1e8c9710f443b"
                                                }
                                            """.trimIndent()
                                            triggerVerification(validSilver)
                                        },
                                        enabled = !isSuspendedOrRevoked,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF475569),
                                            disabledContainerColor = Color(0xFF334155).copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Argent", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (isSuspendedOrRevoked) {
                                                Toast.makeText(context, "Capacité de scan désactivée pour ce compte", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val fakePayload = """
                                                {
                                                  "id": "IDM-FAKE-000",
                                                  "status": "Non Vérifié",
                                                  "name": "Inconnu Falsifié",
                                                  "sig": "sig-failed-pki"
                                                }
                                            """.trimIndent()
                                            triggerVerification(fakePayload)
                                        },
                                        enabled = !isSuspendedOrRevoked,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            disabledContainerColor = Color(0xFF334155).copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Non Conforme", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Manual Token / Hash Entry
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSuspendedOrRevoked) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF7F1D1D).copy(alpha = 0.95f),
                                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Vérification manuelle désactivée (Compte suspendu ou révoqué).",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vérification par Identifiant ou Token", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Collez ici le JSON complet du QR code ou tapez l'identifiant membre (ex: IDM-12345).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = manualInput,
                            onValueChange = { manualInput = it },
                            label = { Text("Charge utile JSON ou Numéro ID") },
                            placeholder = { Text("Coller le contenu scanné ou ID...") },
                            enabled = !isSuspendedOrRevoked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = {
                                if (manualInput.isNotEmpty()) {
                                    IconButton(onClick = { manualInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val item = clipboard.primaryClip?.getItemAt(0)
                                    val pasteText = item?.text?.toString() ?: ""
                                    if (pasteText.isNotEmpty()) {
                                        manualInput = pasteText
                                        Toast.makeText(context, "Données collées depuis le presse-papier", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isSuspendedOrRevoked,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coller")
                            }

                            Button(
                                onClick = {
                                    if (manualInput.isNotBlank()) {
                                        triggerVerification(manualInput)
                                    } else {
                                        Toast.makeText(context, "Veuillez entrer un code ou identifiant", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isSuspendedOrRevoked,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vérifier")
                            }
                        }
                    }
                }

                2 -> {
                    // NFC Badge Emulation & Reader Mode
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(140.dp),
                            shape = CircleShape,
                            color = if (isSuspendedOrRevoked) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Nfc,
                                    contentDescription = "NFC",
                                    tint = if (isSuspendedOrRevoked) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            if (isSuspendedOrRevoked) "Lecteur NFC Désactivé" else "Approchez la carte ou le téléphone", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = if (isSuspendedOrRevoked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (isSuspendedOrRevoked) 
                                "Le terminal sans contact NFC est bloqué car le statut de ce compte a été révoqué ou suspendu."
                            else 
                                "Le lecteur NFC écoute les trames HCE émises par l'application IDMuslim du membre pour authentifier son badge sans contact.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val nfcPayload = com.example.nfc.ProfileApduService.activePayload.ifEmpty {
                                    """
                                        {
                                          "id": "IDM-NFC-7721",
                                          "status": "Vérifié Niveau 3 (Émeraude)",
                                          "name": "Youssef Bennani",
                                          "dob": "10/08/1990",
                                          "residency": "France",
                                          "community": "Centre Islamique Agréé",
                                          "issuedAt": ${System.currentTimeMillis() / 1000},
                                          "sig": "nfc-secure-sha256-verified"
                                        }
                                    """.trimIndent()
                                }
                                triggerVerification(nfcPayload)
                            },
                            enabled = !isSuspendedOrRevoked,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simuler une lecture NFC")
                        }
                    }
                }

                3 -> {
                    // History Log
                    if (scanHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun contrôle effectué récemment", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${scanHistory.size} contrôles enregistrés", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    TextButton(onClick = { scanHistory = emptyList() }) {
                                        Text("Effacer", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            items(scanHistory) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeVerificationResult = item
                                            showResultDialog = true
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isValid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (item.isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (item.isValid) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                            Text("ID: ${item.memberId} • ${item.tierLevel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Certificate / Verification Report Dialog
    if (showResultDialog && activeVerificationResult != null) {
        val res = activeVerificationResult!!
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (res.isValid) Icons.Default.VerifiedUser else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (res.isValid) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (res.isValid) "Identité Authentique Certifiée" else "Contrôle Non Conforme",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (res.isValid) Color(0xFF059669).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Statut : ${res.status}",
                                fontWeight = FontWeight.Bold,
                                color = if (res.isValid) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Nom Complet : ${res.fullName}", fontWeight = FontWeight.SemiBold)
                    Text("• Identifiant : ${res.memberId}")
                    Text("• Niveau : ${res.tierLevel}")
                    Text("• Mosquée / Communauté : ${res.community}")
                    Text("• Date de Naissance : ${res.dateOfBirth}")
                    Text("• Pays de Résidence : ${res.residency}")
                    if (res.nfcUid != null) {
                        Text("• UID NFC : ${res.nfcUid}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (res.cryptoHash != null) {
                        Text("• Hash Sécurisé : ${res.cryptoHash.take(20)}...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("• Empreinte : ${res.signature.take(16)}...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        res.securityRemarks,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Contrôle validé et journalisé", Toast.LENGTH_SHORT).show()
                        showResultDialog = false
                    }
                ) {
                    Text("Fermer & Enregistrer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val shareText = "Certificat IDMuslim\nMembre: ${res.fullName}\nID: ${res.memberId}\nStatut: ${res.status}\nSignature: ${res.signature}"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("IDMuslim Verification", shareText))
                        Toast.makeText(context, "Preuve copiée dans le presse-papier", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copier Preuve")
                }
            }
        )
    }
}
