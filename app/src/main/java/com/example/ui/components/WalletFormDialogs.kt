package com.example.ui.components

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.wallet.SensitiveInfoCategory
import com.example.data.wallet.SensitiveInfoEntity
import com.example.data.wallet.WalletDocCategory
import com.example.data.wallet.WalletDocumentEntity
import com.example.utils.QRCodeGenerator
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDocumentFormDialog(
    initialDocument: WalletDocumentEntity?,
    defaultHolderName: String,
    defaultCountry: String,
    language: String,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        category: String,
        documentNumber: String,
        holderFullName: String,
        issuingCountry: String,
        issuingAuthority: String,
        issueDate: String,
        expiryDate: String,
        mrzCode: String,
        attachmentBase64: String?,
        notes: String,
        isFavorite: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember {
        mutableStateOf(
            initialDocument?.category?.let { WalletDocCategory.fromCode(it) } ?: WalletDocCategory.PASSPORT
        )
    }
    var title by remember { mutableStateOf(initialDocument?.title ?: selectedCategory.localizedLabel(language)) }
    var documentNumber by remember { mutableStateOf(initialDocument?.documentNumber ?: "") }
    var holderName by remember { mutableStateOf(initialDocument?.holderFullName ?: defaultHolderName) }
    var issuingCountry by remember { mutableStateOf(initialDocument?.issuingCountry ?: defaultCountry.ifBlank { "France" }) }
    var issuingAuthority by remember { mutableStateOf(initialDocument?.issuingAuthority ?: "") }
    var issueDate by remember { mutableStateOf(initialDocument?.issueDate ?: "") }
    var expiryDate by remember { mutableStateOf(initialDocument?.expiryDate ?: "") }
    var mrzCode by remember { mutableStateOf(initialDocument?.mrzCode ?: "") }
    var notes by remember { mutableStateOf(initialDocument?.notes ?: "") }
    var attachmentBase64 by remember { mutableStateOf(initialDocument?.attachmentBase64) }
    var isFavorite by remember { mutableStateOf(initialDocument?.isFavorite ?: false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Zero-permission Google Play Policy compliant Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val maxDim = 900
                val ratio = minOf(maxDim.toFloat() / rawBitmap.width, maxDim.toFloat() / rawBitmap.height, 1f)
                val targetW = (rawBitmap.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (rawBitmap.height * ratio).toInt().coerceAtLeast(1)
                val scaled = android.graphics.Bitmap.createScaledBitmap(rawBitmap, targetW, targetH, true)
                val out = ByteArrayOutputStream()
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 82, out)
                attachmentBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            } catch (_: Exception) {
                validationError = "Impossible de lire l'image sélectionnée."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .padding(vertical = 20.dp)
                .testTag("wallet_doc_form_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initialDocument == null) "Nouvelle Pièce d'Identité" else "Modifier la Pièce",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Text(
                    text = "Chiffrée localement via SQLCipher AES-256 + Android Keystore",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WalletDocCategory.entries) { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val prevDefault = selectedCategory.localizedLabel(language)
                                selectedCategory = cat
                                if (title.isBlank() || title == prevDefault) {
                                    title = cat.localizedLabel(language)
                                }
                            },
                            label = { Text(cat.localizedLabel(language)) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("Intitulé du document *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_doc_title_input")
                )

                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it; validationError = null },
                    label = { Text("Numéro officiel de la pièce *") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_doc_number_input")
                )

                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("Nom complet du titulaire *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_doc_holder_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = issuingCountry,
                        onValueChange = { issuingCountry = it },
                        label = { Text("Pays émetteur") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = issuingAuthority,
                        onValueChange = { issuingAuthority = it },
                        label = { Text("Autorité") },
                        placeholder = { Text("Préfecture...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text("Délivré le") },
                        placeholder = { Text("JJ/MM/AAAA") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expire le") },
                        placeholder = { Text("JJ/MM/AAAA") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wallet_doc_expiry_input")
                    )
                }

                OutlinedTextField(
                    value = mrzCode,
                    onValueChange = { mrzCode = it },
                    label = { Text("Zone MRZ / Référence optique (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes sécurisées (optionnel)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Zero-permission Photo Picker attachment card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (attachmentBase64 != null) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (attachmentBase64 != null) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (attachmentBase64 != null) "Copie numérisée jointe (chiffrée)" else "Joindre un scan de la pièce",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Sélecteur sécurisé Android sans accès global galerie",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text(if (attachmentBase64 != null) "Changer" else "Choisir")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Épingler en favori en haut du portefeuille",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || documentNumber.isBlank() || holderName.isBlank()) {
                                validationError = "Veuillez renseigner l'intitulé, le numéro de pièce et le titulaire."
                            } else {
                                onSave(
                                    initialDocument?.id,
                                    title,
                                    selectedCategory.code,
                                    documentNumber,
                                    holderName,
                                    issuingCountry,
                                    issuingAuthority,
                                    issueDate,
                                    expiryDate,
                                    mrzCode,
                                    attachmentBase64,
                                    notes,
                                    isFavorite
                                )
                            }
                        },
                        modifier = Modifier.testTag("wallet_doc_save_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chiffrer & Sauvegarder")
                    }
                }
            }
        }
    }
}

@Composable
fun SensitiveInfoFormDialog(
    initialItem: SensitiveInfoEntity?,
    language: String,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        category: String,
        primaryLabel: String,
        secretValue: String,
        secondaryLabel: String,
        secondaryValue: String,
        confidentialNotes: String,
        isPinned: Boolean,
        requiresBiometricReveal: Boolean
    ) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(
            initialItem?.category?.let { SensitiveInfoCategory.fromCode(it) }
                ?: SensitiveInfoCategory.MEDICAL_EMERGENCY
        )
    }
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var primaryLabel by remember { mutableStateOf(initialItem?.primaryLabel ?: selectedCategory.defaultPrimaryHintFr) }
    var secretValue by remember { mutableStateOf(initialItem?.secretValue ?: "") }
    var showSecretPlaintext by remember { mutableStateOf(false) }
    var secondaryLabel by remember { mutableStateOf(initialItem?.secondaryLabel ?: selectedCategory.defaultSecondaryHintFr) }
    var secondaryValue by remember { mutableStateOf(initialItem?.secondaryValue ?: "") }
    var confidentialNotes by remember { mutableStateOf(initialItem?.confidentialNotes ?: "") }
    var isPinned by remember { mutableStateOf(initialItem?.isPinned ?: false) }
    var requiresBiometric by remember { mutableStateOf(initialItem?.requiresBiometricReveal ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .padding(vertical = 20.dp)
                .testTag("wallet_sensitive_form_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EnhancedEncryption,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initialItem == null) "Nouvelle Info Sensible" else "Modifier le Secret",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SensitiveInfoCategory.entries) { cat ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val prevPrimary = selectedCategory.defaultPrimaryHintFr
                                val prevSecondary = selectedCategory.defaultSecondaryHintFr
                                selectedCategory = cat
                                if (primaryLabel == prevPrimary || primaryLabel.isBlank()) {
                                    primaryLabel = cat.defaultPrimaryHintFr
                                }
                                if (secondaryLabel == prevSecondary || secondaryLabel.isBlank()) {
                                    secondaryLabel = cat.defaultSecondaryHintFr
                                }
                            },
                            label = { Text(cat.localizedLabel(language)) }
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("Titre de l'entrée *") },
                    placeholder = { Text("Ex: Dossier Urgence Médicale, RIB Principal...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_sensitive_title_input")
                )

                OutlinedTextField(
                    value = primaryLabel,
                    onValueChange = { primaryLabel = it },
                    label = { Text("Libellé de la donnée sensible") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = secretValue,
                    onValueChange = { secretValue = it; validationError = null },
                    label = { Text("Valeur confidentielle chiffrée *") },
                    visualTransformation = if (showSecretPlaintext) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showSecretPlaintext = !showSecretPlaintext }) {
                            Icon(
                                imageVector = if (showSecretPlaintext) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Afficher ou masquer la valeur"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_sensitive_secret_input")
                )

                OutlinedTextField(
                    value = secondaryValue,
                    onValueChange = { secondaryValue = it },
                    label = { Text(secondaryLabel.ifBlank { "Information secondaire (optionnel)" }) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confidentialNotes,
                    onValueChange = { confidentialNotes = it },
                    label = { Text("Instructions ou notes confidentielles") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Épingler en priorité", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Exiger confirmation biométrique au dévoilement", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = requiresBiometric, onCheckedChange = { requiresBiometric = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || secretValue.isBlank()) {
                                validationError = "Veuillez renseigner le titre et la valeur confidentielle."
                            } else {
                                onSave(
                                    initialItem?.id,
                                    title,
                                    selectedCategory.code,
                                    primaryLabel.ifBlank { selectedCategory.defaultPrimaryHintFr },
                                    secretValue,
                                    secondaryLabel,
                                    secondaryValue,
                                    confidentialNotes,
                                    isPinned,
                                    requiresBiometric
                                )
                            }
                        },
                        modifier = Modifier.testTag("wallet_sensitive_save_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer dans SQLCipher")
                    }
                }
            }
        }
    }
}

@Composable
fun WalletDocumentDetailDialog(
    document: WalletDocumentEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val qrBitmap = remember(document.id, document.documentNumber) {
        val payload = "IDMUSLIM-VAULT|${document.category}|${document.integrityHashPreview()}|${document.holderFullName}"
        QRCodeGenerator.generateQRCode(payload, 380)
    }

    val decodedAttachment = remember(document.attachmentBase64) {
        document.attachmentBase64?.let { b64 ->
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text(document.title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (decodedAttachment != null) {
                    Image(
                        bitmap = decodedAttachment.asImageBitmap(),
                        contentDescription = "Aperçu numérisé de ${document.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (qrBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR de preuve d'intégrité",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(10.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Titulaire : ${document.holderFullName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("N° Pièce : ${document.documentNumber}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                        Text("Pays / Autorité : ${document.issuingCountry} ${if (document.issuingAuthority.isNotBlank()) "• ${document.issuingAuthority}" else ""}", style = MaterialTheme.typography.bodySmall)
                        if (document.expiryDate.isNotBlank()) {
                            Text("Expiration : ${document.expiryDate}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (document.mrzCode.isNotBlank()) {
                            Text("MRZ : ${document.mrzCode}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                        }
                        if (document.notes.isNotBlank()) {
                            Text("Notes : ${document.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "SHA-256 : ${document.integrityHashPreview()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Modifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun VaultPinDialog(
    isSettingNewPin: Boolean,
    onDismiss: () -> Unit,
    onSubmitPin: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = {
            Text(if (isSettingNewPin) "Définir un Code PIN Coffre" else "Déverrouiller par Code PIN")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isSettingNewPin) {
                        "Choisissez un code PIN de 4 à 8 chiffres (haché en SHA-256 salé) pour déverrouiller le portefeuille."
                    } else {
                        "Saisissez votre code PIN confidentiel pour déverrouiller l'accès SQLCipher."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) { pin = it; errorText = null } },
                    label = { Text("Code PIN (4–8 chiffres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        errorText = "Le code PIN doit comporter au moins 4 chiffres."
                    } else {
                        val ok = onSubmitPin(pin)
                        if (!ok) {
                            errorText = "Code PIN incorrect."
                        }
                    }
                },
                modifier = Modifier.testTag("vault_pin_confirm_button")
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
