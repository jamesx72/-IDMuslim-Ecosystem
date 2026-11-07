package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.wallet.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SqlCipherHeroVaultBanner(
    documentsCount: Int,
    sensitiveCount: Int,
    telemetry: SqlCipherVaultTelemetry?,
    onAddDocument: () -> Unit,
    onAddSensitive: () -> Unit,
    onScanOcr: () -> Unit,
    onImportProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sqlcipher_hero_banner"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF042016),
                            Color(0xFF0A3627),
                            Color(0xFF134E3A)
                        )
                    )
                )
        ) {
            IslamicCardPatternBackground(
                themeIndex = 0,
                modifier = Modifier.matchParentSize(),
                alphaMultiplier = 0.85f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF34D399).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EnhancedEncryption,
                                    contentDescription = "Coffre SQLCipher Actif",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "COFFRE-FORT SQLCIPHER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp
                            )
                            Text(
                                text = "Identité & Données Sensibles",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AES-256",
                                color = Color(0xFFFDE68A),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Metrics strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VaultMetricPill(
                        value = "$documentsCount",
                        label = "Pièces ID"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(Color.White.copy(alpha = 0.16f))
                    )
                    VaultMetricPill(
                        value = "$sensitiveCount",
                        label = "Secrets"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(Color.White.copy(alpha = 0.16f))
                    )
                    VaultMetricPill(
                        value = telemetry?.cipherVersion?.split(" ")?.firstOrNull() ?: "4.5.4",
                        label = "SQLCipher"
                    )
                }

                // Quick actions grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultQuickActionButton(
                        icon = Icons.Default.AddCard,
                        label = "+ Pièce",
                        onClick = onAddDocument,
                        isPrimary = true,
                        testTag = "hero_add_doc_button",
                        modifier = Modifier.weight(1f)
                    )
                    VaultQuickActionButton(
                        icon = Icons.Default.LockPerson,
                        label = "+ Secret",
                        onClick = onAddSensitive,
                        isPrimary = false,
                        testTag = "hero_add_secret_button",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultQuickActionButton(
                        icon = Icons.Default.DocumentScanner,
                        label = "Scanner OCR",
                        onClick = onScanOcr,
                        isPrimary = false,
                        testTag = "hero_scan_ocr_button",
                        modifier = Modifier.weight(1f)
                    )
                    VaultQuickActionButton(
                        icon = Icons.Default.SyncLock,
                        label = "Importer Profil",
                        onClick = onImportProfile,
                        isPrimary = false,
                        testTag = "hero_import_profile_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultMetricPill(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun VaultQuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isPrimary) Color(0xFF10B981) else Color.White.copy(alpha = 0.12f),
        border = if (isPrimary) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color(0xFF022C22) else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isPrimary) Color(0xFF022C22) else Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EncryptedIdentityDocumentCard(
    document: WalletDocumentEntity,
    isRevealed: Boolean,
    language: String,
    onToggleReveal: () -> Unit,
    onCopyNumber: () -> Unit,
    onToggleFavorite: () -> Unit,
    onInspectPass: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = remember(document.category) { WalletDocCategory.fromCode(document.category) }
    val visualTheme = remember(category) { CardVisualThemes.getThemeById(category.cardThemeIndex) }
    val expiry = remember(document.expiryDate) { evaluateExpiryDate(document.expiryDate) }

    val statusColor = when (expiry.status) {
        ExpiryHealthStatus.VALID, ExpiryHealthStatus.NO_EXPIRY -> Color(0xFF34D399)
        ExpiryHealthStatus.EXPIRING_SOON -> Color(0xFFFBBF24)
        ExpiryHealthStatus.EXPIRED -> Color(0xFFF87171)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspectPass() }
            .testTag("wallet_doc_card_${document.id}"),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(visualTheme.gradientColors))
        ) {
            IslamicCardPatternBackground(
                themeIndex = visualTheme.id,
                modifier = Modifier.matchParentSize(),
                alphaMultiplier = 0.7f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top row: Category chip + Expiry & Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(visualTheme.accentColor.copy(alpha = 0.2f))
                                .border(1.dp, visualTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = visualTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = category.localizedLabel(language).uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall,
                                color = visualTheme.accentColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.65f))
                        ) {
                            Text(
                                text = expiry.labelFr,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Épingler en favori",
                                tint = if (document.isFavorite) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Middle: Encrypted Document Number Display
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NUMÉRO DE PIÈCE CHIFFRÉ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isRevealed) document.documentNumber else document.maskedDocumentNumber(),
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row {
                            IconButton(
                                onClick = onToggleReveal,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("reveal_doc_${document.id}")
                            ) {
                                Icon(
                                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isRevealed) "Masquer le numéro" else "Révéler le numéro",
                                    tint = visualTheme.accentColor
                                )
                            }
                            IconButton(
                                onClick = onCopyNumber,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier le numéro de manière sécurisée",
                                    tint = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Holder & Country Metadata + Action Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = document.holderFullName.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = buildString {
                                append(document.issuingCountry.ifBlank { "International" })
                                if (document.attachmentBase64 != null) append(" • Scan joint")
                                append(" • #${document.integrityHashPreview().take(6)}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onInspectPass,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = "Afficher le QR de preuve", tint = Color.White)
                        }
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier la pièce", tint = Color.White.copy(alpha = 0.85f))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer la pièce", tint = Color(0xFFFCA5A5))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EncryptedSensitiveInfoCard(
    item: SensitiveInfoEntity,
    isRevealed: Boolean,
    language: String,
    onToggleReveal: () -> Unit,
    onCopySecret: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = remember(item.category) { SensitiveInfoCategory.fromCode(item.category) }
    val (categoryIcon, accentColor) = remember(category) {
        when (category) {
            SensitiveInfoCategory.MEDICAL_EMERGENCY -> Icons.Default.MedicalServices to Color(0xFFEF4444)
            SensitiveInfoCategory.BANKING_FINANCE -> Icons.Default.AccountBalance to Color(0xFF10B981)
            SensitiveInfoCategory.GOVERNMENT_ADMIN -> Icons.Default.Gavel to Color(0xFF3B82F6)
            SensitiveInfoCategory.SECURITY_PIN_2FA -> Icons.Default.Password to Color(0xFFF59E0B)
            SensitiveInfoCategory.CONFIDENTIAL_NOTE -> Icons.Default.Lock to Color(0xFF8B5CF6)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wallet_sensitive_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
        border = BorderStroke(
            width = if (item.isPinned) 1.5.dp else 1.dp,
            color = if (item.isPinned) accentColor.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(categoryIcon, contentDescription = null, tint = accentColor)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.localizedLabel(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Default.BookmarkBorder,
                        contentDescription = "Épingler",
                        tint = if (item.isPinned) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.primaryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isRevealed) item.secretValue else item.maskedSecretValue(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AnimatedVisibility(visible = isRevealed && item.secondaryValue.isNotBlank()) {
                            Text(
                                text = "${item.secondaryLabel.ifBlank { "Complément" }} : ${item.secondaryValue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        AnimatedVisibility(visible = isRevealed && item.confidentialNotes.isNotBlank()) {
                            Text(
                                text = item.confidentialNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onToggleReveal,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("reveal_secret_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isRevealed) "Masquer le secret" else "Dévoiler le secret",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onCopySecret,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier dans le presse-papier privé",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.requiresBiometricReveal) Icons.Default.Fingerprint else Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isRevealed) "Masquage auto dans 15s" else "SQLCipher + AES-GCM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    TextButton(onClick = onEdit) {
                        Text("Modifier")
                    }
                    TextButton(onClick = onDelete) {
                        Text("Supprimer", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SqlCipherAuditPanel(
    telemetry: SqlCipherVaultTelemetry?,
    isScreenCaptureBlocked: Boolean,
    hasCustomPin: Boolean,
    isRotatingKey: Boolean,
    onToggleScreenCapture: (Boolean) -> Unit,
    onConfigurePin: () -> Unit,
    onRotateKey: () -> Unit,
    onPurgeVault: () -> Unit
) {
    val rotatedDateStr = remember(telemetry?.keyLastRotatedAt) {
        val ts = telemetry?.keyLastRotatedAt ?: System.currentTimeMillis()
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts))
    }
    val sizeKb = remember(telemetry?.databaseSizeBytes) {
        val bytes = telemetry?.databaseSizeBytes ?: 0L
        if (bytes == 0L) "Initialisé" else "${(bytes / 1024L).coerceAtLeast(1L)} Ko"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sqlcipher_audit_panel"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Télémétrie Cryptographique SQLCipher", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Vérification en direct du moteur et de l'en-tête binaire", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                AuditKeyValueRow("Moteur SQLCipher", telemetry?.cipherVersion ?: "4.5.4")
                AuditKeyValueRow("Suite Cryptographique", telemetry?.cipherAlgorithm ?: "AES-256-CBC + HMAC-SHA512")
                AuditKeyValueRow("Dérivation PBKDF2", "${telemetry?.kdfIterations ?: 256000} itérations")
                AuditKeyValueRow("Taille de page chiffrée", "${telemetry?.pageSize ?: 4096} octets")
                AuditKeyValueRow("Base locale", "${telemetry?.databaseFileName ?: "idmuslim_wallet_vault.db"} ($sizeKb)")
                AuditKeyValueRow("Rotation de clé", rotatedDateStr)

                Text(
                    text = "Empreinte SHA-256 Clé Maîtresse (AndroidKeyStore) :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = telemetry?.keyFingerprintSha256 ?: "Calcul...",
                        color = Color(0xFF34D399),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "En-tête brut sur disque (16 premiers octets — Zéro texte clair SQLite) :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = telemetry?.encryptedHeaderHexPreview ?: "Vérification...",
                        color = Color(0xFFFBBF24),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Security Hardening Controls
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bouclier Anti-Capture d'Écran (FLAG_SECURE)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Bloque les captures d'écran et l'enregistrement vidéo sur cet écran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isScreenCaptureBlocked, onCheckedChange = onToggleScreenCapture)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Code PIN de Secours du Coffre", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (hasCustomPin) "Configuré (SHA-256 salé)" else "Non défini • Recommandé en secours de la biométrie",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onConfigurePin,
                        modifier = Modifier.testTag("configure_vault_pin_button")
                    ) {
                        Text(if (hasCustomPin) "Modifier" else "Définir")
                    }
                }

                Button(
                    onClick = onRotateKey,
                    enabled = !isRotatingKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("sqlcipher_rekey_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isRotatingKey) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.SyncLock, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rotation de Clé SQLCipher (PRAGMA rekey)")
                }

                OutlinedButton(
                    onClick = onPurgeVault,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("sqlcipher_purge_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Purge Cryptographique d'Urgence")
                }
            }
        }
    }
}

@Composable
private fun AuditKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
