package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.PdfGenerator
import java.io.File
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurePdfExportDialog(
    fullName: String,
    dateOfBirth: String,
    residency: String,
    community: String,
    passport: String,
    license: String,
    memberId: String,
    verificationStatus: String = "CERTIFIÉ / VALIDE",
    expiryDate: String = "2029-12-31",
    photoBase64: String? = null,
    viewModel: EventViewModel? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isPasswordProtected by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("IDM-${(1000..9999).random()}-SEC") }
    var isPasswordVisible by remember { mutableStateOf(true) }
    var selectedLayoutMode by remember { mutableStateOf(PdfGenerator.PrintLayoutMode.CARD_CUTOUT_WALLET) }
    var selectedColorScheme by remember { mutableStateOf(PdfGenerator.PdfColorScheme.EMERALD_GOLD) }
    var includePhoto by remember { mutableStateOf(true) }
    var includeQrCode by remember { mutableStateOf(true) }
    var includeBarcode by remember { mutableStateOf(true) }
    var customNote by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var generationError by remember { mutableStateOf<String?>(null) }

    fun generatePassword(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part1 = (1000..9999).random()
        val part2 = (1..4).map { chars.random() }.joinToString("")
        return "IDM-$part1-$part2"
    }

    Dialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0F5A47).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFF0F5A47),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Génération PDF Sécurisé",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Impression physique & Stockage chiffré",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isGenerating,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (generatedFile != null) {
                        // Success View
                        val file = generatedFile!!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF059669).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PDF Généré avec Succès !",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fichier : ${file.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Taille : ${(file.length() / 1024) + 1} Ko • Emplacement : Téléchargements",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isPasswordProtected && password.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Mot de passe de protection :",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = password,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(password))
                                                    Toast.makeText(context, "Mot de passe copié !", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { PdfGenerator.printPdf(context, file) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Imprimer")
                                    }

                                    OutlinedButton(
                                        onClick = { PdfGenerator.sharePdf(context, file, isPasswordProtected) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Partager")
                                    }
                                }
                            }
                        }
                    }

                    if (generationError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generationError!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 1. Format Selection
                    Text(
                        text = "1. Format du Document",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FormatOptionCard(
                            title = "Carte Découpable",
                            subtitle = "Recto/Verso avec repères pliage",
                            icon = Icons.Default.CreditCard,
                            isSelected = selectedLayoutMode == PdfGenerator.PrintLayoutMode.CARD_CUTOUT_WALLET,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLayoutMode = PdfGenerator.PrintLayoutMode.CARD_CUTOUT_WALLET }
                        )

                        FormatOptionCard(
                            title = "Certificat A4",
                            subtitle = "Attestation officielle complète",
                            icon = Icons.Default.Description,
                            isSelected = selectedLayoutMode == PdfGenerator.PrintLayoutMode.OFFICIAL_CERTIFICATE,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLayoutMode = PdfGenerator.PrintLayoutMode.OFFICIAL_CERTIFICATE }
                        )
                    }

                    // 2. Password Protection Card
                    Text(
                        text = "2. Sécurité & Chiffrement",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPasswordProtected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, if (isPasswordProtected) Color(0xFF0F5A47).copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isPasswordProtected) Color(0xFF0F5A47) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Protéger par Mot de Passe",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Chiffrement standard PDF (AES / RC4)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isPasswordProtected,
                                    onCheckedChange = { isPasswordProtected = it }
                                )
                            }

                            AnimatedVisibility(visible = isPasswordProtected) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Mot de passe du fichier PDF") },
                                        singleLine = true,
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Afficher/Masquer"
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { password = generatePassword() },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Générer un mot de passe fort", fontSize = 12.sp)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (password.length >= 8) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (password.length >= 8) "Sécurité Forte" else "Sécurité Moyenne",
                                                color = if (password.length >= 8) Color(0xFF059669) else Color(0xFFD97706),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Ce mot de passe sera requis par toute visionneuse PDF ou imprimante pour déverrouiller le document.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 3. Visual & Print Options
                    Text(
                        text = "3. Thème & Éléments Graphiques",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePill(
                            title = "Émeraude",
                            color = Color(0xFF0F5A47),
                            isSelected = selectedColorScheme == PdfGenerator.PdfColorScheme.EMERALD_GOLD,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedColorScheme = PdfGenerator.PdfColorScheme.EMERALD_GOLD }
                        )

                        ThemePill(
                            title = "Bleu Nuit",
                            color = Color(0xFF1E293B),
                            isSelected = selectedColorScheme == PdfGenerator.PdfColorScheme.NAVY_PLATINUM,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedColorScheme = PdfGenerator.PdfColorScheme.NAVY_PLATINUM }
                        )

                        ThemePill(
                            title = "Monochrome",
                            color = Color(0xFF333333),
                            isSelected = selectedColorScheme == PdfGenerator.PdfColorScheme.MONOCHROME_PRINT,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedColorScheme = PdfGenerator.PdfColorScheme.MONOCHROME_PRINT }
                        )
                    }

                    // Toggles
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inclure Photo de Profil", style = MaterialTheme.typography.bodySmall)
                                Checkbox(checked = includePhoto, onCheckedChange = { includePhoto = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inclure QR Code de Vérification", style = MaterialTheme.typography.bodySmall)
                                Checkbox(checked = includeQrCode, onCheckedChange = { includeQrCode = it })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inclure Code-barres Membre", style = MaterialTheme.typography.bodySmall)
                                Checkbox(checked = includeBarcode, onCheckedChange = { includeBarcode = it })
                            }
                        }
                    }

                    // Preview Metadata Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aperçu des Données Incluses", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Titulaire : $fullName ($memberId)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Statut : $verificationStatus • Expiration : $expiryDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• Format : A4 Haute Résolution (Prêt pour découpe & plastification)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Footer Actions
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isGenerating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fermer")
                    }

                    Button(
                        onClick = {
                            if (isPasswordProtected && password.isBlank()) {
                                Toast.makeText(context, "Veuillez saisir un mot de passe ou désactiver la protection", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isGenerating = true
                            generationError = null

                            val options = PdfGenerator.PdfGenerationOptions(
                                password = if (isPasswordProtected) password else "",
                                isPasswordProtected = isPasswordProtected,
                                layoutMode = selectedLayoutMode,
                                colorScheme = selectedColorScheme,
                                includePhoto = includePhoto,
                                includeQrCode = includeQrCode,
                                includeBarcode = includeBarcode,
                                customNote = customNote
                            )

                            PdfGenerator.generateSecurePdf(
                                context = context,
                                fullName = fullName,
                                dateOfBirth = dateOfBirth,
                                residency = residency,
                                community = community,
                                passport = passport,
                                license = license,
                                memberId = memberId,
                                verificationStatus = verificationStatus,
                                expiryDate = expiryDate,
                                photoBase64 = photoBase64,
                                options = options,
                                onSuccess = { file ->
                                    isGenerating = false
                                    generatedFile = file
                                    viewModel?.logActivity("Génération PDF Sécurisé", "PDF chiffré généré : ${file.name}")
                                    Toast.makeText(context, "PDF sauvegardé dans Téléchargements !", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    isGenerating = false
                                    generationError = err
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isGenerating,
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5A47))
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chiffrement...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer le PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF0F5A47).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) Color(0xFF0F5A47) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF0F5A47) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF0F5A47),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF0F5A47) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemePill(
    title: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        }
    }
}
