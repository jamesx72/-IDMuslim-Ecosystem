package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.wallet.EncryptedVaultFileEntity
import com.example.data.wallet.WalletDocCategory
import com.example.ui.viewmodels.VaultFilePreviewState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EncryptedDocumentUploadHeaderCard(
    eccFingerprint: String,
    fileCount: Int,
    isUploading: Boolean,
    onPickPdfOrFile: () -> Unit,
    onPickImagePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("encrypted_doc_upload_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF062E22), Color(0xFF0F3D2E), Color(0xFF164E63))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF34D399).copy(alpha = 0.18f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Coffre Documents PDF et Images Chiffrés",
                                    tint = Color(0xFF6EE7B7)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "DÉPÔT DOCUMENTAIRE CHIFFRÉ E2E",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PDF & Photos d'Identité",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "$fileCount fichier(s)",
                            color = Color(0xFFFDE68A),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Text(
                    text = "Chiffrement local ECC P-256 (ECIES + AES-256-GCM) et signature ECDSA avant envoi vers Firebase Storage + indexation SQLCipher.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                if (eccFingerprint.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Clé ECC P-256 : $eccFingerprint",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFA7F3D0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF34D399),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPickPdfOrFile,
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("upload_encrypted_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF / Fichier", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onPickImagePhoto,
                        enabled = !isUploading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("upload_encrypted_image_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Image ID", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EncryptedVaultFileCard(
    file: EncryptedVaultFileEntity,
    language: String,
    onDecryptAndOpen: () -> Unit,
    onRetryCloudSync: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSyncedCloud = file.syncStatus.equals("SYNCED_CLOUD", ignoreCase = true)
    val categoryLabel = remember(file.docCategory, language) {
        WalletDocCategory.fromCode(file.docCategory).localizedLabel(language)
    }
    val dateStr = remember(file.updatedAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.updatedAt))
    }
    val sizeLabel = remember(file.originalSizeBytes) {
        val kb = (file.originalSizeBytes / 1024L).coerceAtLeast(1L)
        if (kb >= 1024L) String.format(Locale.US, "%.1f Mo", kb / 1024.0) else "$kb Ko"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("encrypted_file_card_${file.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (file.isPdf) Color(0xFFDC2626).copy(alpha = 0.14f) else Color(0xFF2563EB).copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (file.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                contentDescription = null,
                                tint = if (file.isPdf) Color(0xFFEF4444) else Color(0xFF3B82F6)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = file.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$categoryLabel • $sizeLabel • $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSyncedCloud) Color(0xFF059669).copy(alpha = 0.16f) else Color(0xFFD97706).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, if (isSyncedCloud) Color(0xFF10B981) else Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSyncedCloud) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isSyncedCloud) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isSyncedCloud) "Cloud + Local" else "Local Chiffré",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSyncedCloud) Color(0xFF10B981) else Color(0xFFD97706)
                        )
                    }
                }
            }

            // Cryptographic envelope audit chip row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${file.eccAlgorithm} • ECDSA Signé",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "SHA-256 : ${file.shortDigestPreview()}… • ECC : ${file.eccPublicKeyFingerprint.take(17)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDecryptAndOpen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("decrypt_open_file_${file.id}")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Déchiffrer", fontWeight = FontWeight.Bold)
                }

                if (!isSyncedCloud) {
                    OutlinedButton(
                        onClick = onRetryCloudSync,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Synchroniser Firebase Storage", modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_encrypted_file_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer le fichier chiffré",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EncryptedUploadStagingDialog(
    initialTitle: String,
    language: String,
    onDismiss: () -> Unit,
    onConfirmUpload: (title: String, categoryCode: String, syncToFirebase: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var selectedCategory by remember { mutableStateOf(WalletDocCategory.NATIONAL_ID) }
    var syncToFirebase by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.EnhancedEncryption,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Chiffrer & Déposer le Document") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Libellé du document") },
                    placeholder = { Text("Ex: Passeport Biométrique PDF") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("staging_doc_title_input")
                )

                Text(
                    text = "Catégorie",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WalletDocCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.localizedLabel(language)) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sauvegarde Firebase Storage (.enc)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Seuls les octets chiffrés ECC/AES-256-GCM sont transmis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = syncToFirebase,
                        onCheckedChange = { syncToFirebase = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmUpload(title, selectedCategory.code, syncToFirebase) },
                modifier = Modifier.testTag("confirm_encrypted_upload_button")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chiffrer & Envoyer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EncryptedDocumentPreviewDialog(
    previewState: VaultFilePreviewState,
    onDismiss: () -> Unit
) {
    if (previewState is VaultFilePreviewState.Hidden) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when (previewState) {
                        is VaultFilePreviewState.Loading -> previewState.file.title
                        is VaultFilePreviewState.ImageReady -> previewState.file.title
                        is VaultFilePreviewState.PdfReady -> previewState.file.title
                        is VaultFilePreviewState.Error -> previewState.file.title
                        VaultFilePreviewState.Hidden -> ""
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Déchiffrement ECC en mémoire • Capture bloquée",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer l'aperçu")
                    }
                }

                HorizontalDivider()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (previewState) {
                        is VaultFilePreviewState.Loading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Déchiffrement ECIES P-256 & vérification SHA-256...")
                            }
                        }
                        is VaultFilePreviewState.ImageReady -> {
                            Image(
                                bitmap = previewState.bitmap.asImageBitmap(),
                                contentDescription = previewState.file.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        is VaultFilePreviewState.PdfReady -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(previewState.pages.withIndex().toList(), key = { it.index }) { (idx, bmp: Bitmap) ->
                                    Column {
                                        Text(
                                            text = "Page ${idx + 1} / ${previewState.pages.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Page ${idx + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                        is VaultFilePreviewState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.GppBad,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Échec du déchiffrement",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = previewState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        VaultFilePreviewState.Hidden -> Unit
                    }
                }
            }
        }
    }
}
