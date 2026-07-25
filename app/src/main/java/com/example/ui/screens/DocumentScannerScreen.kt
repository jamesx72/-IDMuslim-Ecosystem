package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.ui.viewmodels.EventViewModel
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
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isCropApplied by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    if (capturedUri != null) {
        // Dedicated Image Preview, Crop, Rotate, and Confirmation Screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Aperçu du document") },
                    navigationIcon = {
                        IconButton(onClick = { capturedUri = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Reprendre")
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
                        contentDescription = "Aperçu de la photo",
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
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.75f)
                            .border(
                                width = 2.dp,
                                color = if (isCropApplied) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
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
                        Text(
                            text = "Vérifiez que toutes les informations du document sont bien lisibles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Retake photo button
                            OutlinedButton(
                                onClick = {
                                    capturedUri = null
                                    rotationAngle = 0f
                                    isCropApplied = false
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reprendre")
                            }

                            // Rotate button
                            IconButton(
                                onClick = { rotationAngle = (rotationAngle + 90f) % 360f }
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

                        // Submit final document
                        Button(
                            onClick = {
                                if (isUploading) return@Button
                                isUploading = true
                                val uri = capturedUri ?: return@Button

                                // Process rotation and crop on bitmap before uploading
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
                                Text("Valider & Envoyer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Scanner un document") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            if (hasPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor: Executor = ContextCompat.getMainExecutor(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
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
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
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
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Autorisation de la caméra requise.")
                }
            }
        }
    }
}

