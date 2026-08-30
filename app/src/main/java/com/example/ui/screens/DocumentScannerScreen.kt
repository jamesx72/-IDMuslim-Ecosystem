package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.DocumentOcrProcessor
import com.example.utils.ExtractedIdCredentials
import com.example.utils.HapticHelper
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var isScanningOcr by remember { mutableStateOf(false) }

    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isCropApplied by remember { mutableStateOf(false) }
    var extractedOcrData by remember { mutableStateOf<ExtractedIdCredentials?>(null) }
    var showOcrDetailsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            onNavigateBack()
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedUri = uri
            // Immediately run OCR on picked document
            coroutineScope.launch {
                isScanningOcr = true
                try {
                    val result = DocumentOcrProcessor.extractCredentialsFromImageUri(context, uri)
                    extractedOcrData = result
                } catch (e: Exception) {
                    Log.e("ScannerOCR", "OCR Failed", e)
                } finally {
                    isScanningOcr = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // Trigger OCR automatically when a new photo is captured
    LaunchedEffect(capturedUri) {
        val uri = capturedUri
        if (uri != null) {
            isScanningOcr = true
            try {
                val result = DocumentOcrProcessor.extractCredentialsFromImageUri(context, uri)
                extractedOcrData = result
                if (result.fullName.isNotBlank() || result.docNumber.isNotBlank()) {
                    HapticHelper.performSuccess(context, haptic)
                }
            } catch (e: Exception) {
                Log.e("ScannerOCR", "Error extracting text", e)
            } finally {
                isScanningOcr = false
            }
        } else {
            extractedOcrData = null
        }
    }

    if (capturedUri != null) {
        // Dedicated Image Preview, OCR Recognition, Crop, Rotate, and Auto-Fill Screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Aperçu & Reconnaissance OCR") },
                    navigationIcon = {
                        IconButton(onClick = { capturedUri = null; extractedOcrData = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Reprendre")
                        }
                    },
                    actions = {
                        if (extractedOcrData != null && (extractedOcrData?.fullName?.isNotBlank() == true || extractedOcrData?.docNumber?.isNotBlank() == true)) {
                            IconButton(onClick = { showOcrDetailsDialog = true }) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = "Détails OCR", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image preview container with document border overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(capturedUri),
                        contentDescription = "Aperçu de la pièce",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                rotationZ = rotationAngle,
                                scaleX = if (isCropApplied) 1.1f else 1.0f,
                                scaleY = if (isCropApplied) 1.1f else 1.0f
                            ),
                        contentScale = ContentScale.Fit
                    )

                    // Visual document scanner frame guidelines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.75f)
                            .border(
                                width = 2.dp,
                                color = if (extractedOcrData?.fullName?.isNotBlank() == true) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )

                    if (isScanningOcr) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF34D399), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Reconnaissance de texte OCR en cours...", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Controls & Action Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // OCR Detected Summary Pill
                        extractedOcrData?.let { ocr ->
                            if (ocr.fullName.isNotBlank() || ocr.docNumber.isNotBlank() || ocr.dateOfBirth.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Données Détectées par OCR (Prêtes pour auto-remplissage)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF065F46))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (ocr.fullName.isNotBlank()) {
                                            Text("• Nom : ${ocr.fullName}", fontSize = 12.sp, color = Color(0xFF064E3B))
                                        }
                                        if (ocr.docNumber.isNotBlank()) {
                                            Text("• N° Doc : ${ocr.docNumber} (${ocr.docType})", fontSize = 12.sp, color = Color(0xFF064E3B))
                                        }
                                        if (ocr.dateOfBirth.isNotBlank()) {
                                            Text("• Date de naissance : ${ocr.dateOfBirth}", fontSize = 12.sp, color = Color(0xFF064E3B))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Retake photo button
                            OutlinedButton(
                                onClick = {
                                    capturedUri = null
                                    rotationAngle = 0f
                                    isCropApplied = false
                                    extractedOcrData = null
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reprendre")
                            }

                            // Rotate button
                            IconButton(
                                onClick = {
                                    rotationAngle = (rotationAngle + 90f) % 360f
                                }
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = "Pivoter", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Toggle crop frame button
                            IconButton(
                                onClick = { isCropApplied = !isCropApplied }
                            ) {
                                Icon(
                                    Icons.Default.Crop,
                                    contentDescription = "Cadrer",
                                    tint = if (isCropApplied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Submit & Auto-Fill button
                        Button(
                            onClick = {
                                if (isUploading) return@Button
                                isUploading = true
                                val uri = capturedUri ?: return@Button

                                // Auto-fill profile fields if detected by OCR
                                extractedOcrData?.let { ocr ->
                                    val sessionManager = com.example.network.ApiClient.getSessionManager()
                                    if (ocr.fullName.isNotBlank()) {
                                        sessionManager.saveProfileFullName(ocr.fullName)
                                    }
                                    if (ocr.dateOfBirth.isNotBlank()) {
                                        sessionManager.saveProfileDob(ocr.dateOfBirth)
                                    }
                                    if (ocr.docNumber.isNotBlank()) {
                                        sessionManager.saveDocNumber(ocr.docNumber)
                                        if (ocr.docType.contains("Passport", ignoreCase = true)) {
                                            sessionManager.savePassportNumber(ocr.docNumber)
                                        }
                                    }
                                    if (ocr.docType.isNotBlank()) {
                                        sessionManager.saveDocType(ocr.docType)
                                    }
                                    if (ocr.issuingCountry.isNotBlank()) {
                                        sessionManager.saveIssuingCountry(ocr.issuingCountry)
                                    }
                                    if (ocr.expiryDate.isNotBlank()) {
                                        sessionManager.saveExpiryDate(ocr.expiryDate)
                                    }
                                }

                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                                    inputStream?.close()

                                    if (originalBitmap != null) {
                                        val matrix = Matrix()
                                        if (rotationAngle != 0f) {
                                            matrix.postRotate(rotationAngle)
                                        }

                                        var processedBitmap = Bitmap.createBitmap(
                                            originalBitmap, 0, 0,
                                            originalBitmap.width, originalBitmap.height,
                                            matrix, true
                                        )

                                        if (isCropApplied) {
                                            val cropX = (processedBitmap.width * 0.05).toInt()
                                            val cropY = (processedBitmap.height * 0.05).toInt()
                                            val cropW = (processedBitmap.width * 0.90).toInt()
                                            val cropH = (processedBitmap.height * 0.90).toInt()
                                            processedBitmap = Bitmap.createBitmap(
                                                processedBitmap, cropX, cropY, cropW, cropH
                                            )
                                        }

                                        val outputFile = File(
                                            context.cacheDir,
                                            "processed_doc_${System.currentTimeMillis()}.jpg"
                                        )
                                        val fos = FileOutputStream(outputFile)
                                        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                                        fos.flush()
                                        fos.close()

                                        val finalUri = Uri.fromFile(outputFile)
                                        viewModel.uploadDocumentForVerification(finalUri, context)
                                    } else {
                                        viewModel.uploadDocumentForVerification(uri, context)
                                    }
                                } catch (e: Exception) {
                                    Log.e("Preview", "Error processing photo", e)
                                    viewModel.uploadDocumentForVerification(uri, context)
                                } finally {
                                    isUploading = false
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Valider & Remplir les Identifiants", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Full Raw Text OCR Inspect Dialog
        if (showOcrDetailsDialog && extractedOcrData != null) {
            AlertDialog(
                onDismissRequest = { showOcrDetailsDialog = false },
                title = { Text("Texte Intégral Détecté (ML Kit OCR)") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = extractedOcrData?.rawText ?: "Aucun texte détecté.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showOcrDetailsDialog = false }) {
                        Text("Fermer")
                    }
                }
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Scanner un document")
                            Text("Reconnaissance automatique OCR", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    actions = {
                        IconButton(onClick = { galleryPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Galerie", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            DisposableEffect(lifecycleOwner) {
                onDispose {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            if (hasPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            val executor: Executor = ContextCompat.getMainExecutor(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (exc: Exception) {
                                    Log.e("CameraX", "Use case binding failed", exc)
                                }
                            }, executor)
                            previewView
                        },
                        onRelease = {
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                cameraProvider.unbindAll()
                            } catch (e: Exception) {
                                // ignore
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Target Frame Visual Guide
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.58f) // ID-1 / Credit Card standard aspect ratio
                                .border(2.dp, Color(0xFF34D399), RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Placez la pièce d'identité ou passeport dans le cadre",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Bottom Shutter & Gallery Controls
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            IconButton(
                                onClick = { galleryPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Galerie", tint = Color.White)
                            }

                            if (isCapturing) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            } else {
                                IconButton(
                                    onClick = {
                                        isCapturing = true
                                        val photoFile = File(
                                            context.cacheDir,
                                            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
                                        )
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                        imageCapture.takePicture(
                                            outputOptions,
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onError(exc: ImageCaptureException) {
                                                    Log.e("CameraX", "Photo capture failed: ${exc.message}", exc)
                                                    isCapturing = false
                                                }

                                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                    val savedUri = Uri.fromFile(photoFile)
                                                    capturedUri = savedUri
                                                    isCapturing = false
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                        .padding(8.dp)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Prendre une photo",
                                        tint = Color.Black,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.size(52.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Autorisation de la caméra requise pour le scan OCR.")
                }
            }
        }
    }
}
