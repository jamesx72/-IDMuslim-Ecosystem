package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.wallet.*
import com.example.ui.components.*
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.DigitalWalletViewModel
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.HapticHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalWalletScreen(
    eventViewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocumentScanner: () -> Unit = {},
    walletViewModel: DigitalWalletViewModel = viewModel(
        factory = DigitalWalletViewModel.Factory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val uiState by walletViewModel.uiState.collectAsStateWithLifecycle()
    val documents by walletViewModel.documents.collectAsStateWithLifecycle()
    val sensitiveItems by walletViewModel.sensitiveItems.collectAsStateWithLifecycle()
    val encryptedFiles by walletViewModel.encryptedFiles.collectAsStateWithLifecycle()
    val previewState by walletViewModel.previewState.collectAsStateWithLifecycle()

    val language by eventViewModel.language.collectAsStateWithLifecycle()
    val cachedProfile by eventViewModel.cachedUserProfile.collectAsStateWithLifecycle()
    val cachedScannedDocs by eventViewModel.cachedDocuments.collectAsStateWithLifecycle()
    val profileFullName by eventViewModel.profileFullName.collectAsStateWithLifecycle()
    val profilePassport by eventViewModel.profilePassportNumber.collectAsStateWithLifecycle()
    val profileLicense by eventViewModel.profileLicenseNumber.collectAsStateWithLifecycle()
    val profileDocType by eventViewModel.profileDocType.collectAsStateWithLifecycle()
    val profileDocNumber by eventViewModel.profileDocNumber.collectAsStateWithLifecycle()
    val profileCountry by eventViewModel.profileIssuingCountry.collectAsStateWithLifecycle()
    val profileExpiry by eventViewModel.profileExpiryDate.collectAsStateWithLifecycle()
    val isDeviceCompromised by eventViewModel.isDeviceCompromised.collectAsStateWithLifecycle()

    var showDocumentDialog by remember { mutableStateOf(false) }
    var editingDocument by remember { mutableStateOf<WalletDocumentEntity?>(null) }
    var inspectingDocument by remember { mutableStateOf<WalletDocumentEntity?>(null) }

    var showSensitiveDialog by remember { mutableStateOf(false) }
    var editingSensitive by remember { mutableStateOf<SensitiveInfoEntity?>(null) }

    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var pendingUploadTitle by remember { mutableStateOf("") }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPinUnlockDialog by remember { mutableStateOf(false) }
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }

    // Play-Policy compliant document & photo pickers (no READ_EXTERNAL_STORAGE / READ_MEDIA_IMAGES needed)
    val pdfAndDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUploadTitle = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Document Identité"
            pendingUploadUri = uri
        }
    }

    val photoIdPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUploadTitle = "Photo Pièce d'Identité"
            pendingUploadUri = uri
        }
    }

    BackHandler {
        onNavigateBack()
    }

    // Apply FLAG_SECURE to prevent screenshots & screen recording of decrypted wallet credentials
    DisposableEffect(uiState.isScreenCaptureBlocked) {
        val window = (context as? Activity)?.window
        if (uiState.isScreenCaptureBlocked) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            val keepGlobalSecure = eventViewModel.screenSecurityEnabled.value
            if (!keepGlobalSecure) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    val filteredDocuments = remember(documents, uiState.searchQuery, uiState.selectedDocCategory) {
        documents.filter { doc ->
            val matchesCat = uiState.selectedDocCategory == null || doc.category == uiState.selectedDocCategory
            val q = uiState.searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                doc.title.contains(q, ignoreCase = true) ||
                doc.holderFullName.contains(q, ignoreCase = true) ||
                doc.issuingCountry.contains(q, ignoreCase = true) ||
                doc.documentNumber.contains(q, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    val filteredSensitiveItems = remember(sensitiveItems, uiState.searchQuery, uiState.selectedInfoCategory) {
        sensitiveItems.filter { item ->
            val matchesCat = uiState.selectedInfoCategory == null || item.category == uiState.selectedInfoCategory
            val q = uiState.searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                item.title.contains(q, ignoreCase = true) ||
                item.primaryLabel.contains(q, ignoreCase = true) ||
                item.confidentialNotes.contains(q, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    val requestBiometricThen = { action: () -> Unit ->
        scope.launch {
            val authenticated = com.example.utils.BiometricHelper.authenticate(context)
            if (authenticated) {
                HapticHelper.performAuthSuccess(context, haptic)
                action()
            } else {
                Toast.makeText(context, "Authentification requise annulée.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Translations.get(language, "wallet_title"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Translations.get(language, "wallet_subtitle"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("wallet_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            walletViewModel.toggleGlobalPrivacyMask()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("wallet_privacy_mask_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPrivacyMaskEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Basculer le mode masqué",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            if (uiState.isVaultLocked) {
                                requestBiometricThen { walletViewModel.unlockVault() }
                            } else {
                                walletViewModel.lockVault()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("wallet_lock_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isVaultLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = Translations.get(language, "wallet_lock_now"),
                            tint = if (uiState.isVaultLocked) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isVaultLocked && uiState.selectedTabIndex != 2) {
                ExtendedFloatingActionButton(
                    onClick = {
                        HapticHelper.performClick(context, haptic)
                        when (uiState.selectedTabIndex) {
                            0 -> {
                                editingDocument = null
                                showDocumentDialog = true
                            }
                            1 -> {
                                editingSensitive = null
                                showSensitiveDialog = true
                            }
                            3 -> {
                                pdfAndDocPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = when (uiState.selectedTabIndex) {
                                0 -> Icons.Default.AddCard
                                3 -> Icons.Default.UploadFile
                                else -> Icons.Default.EnhancedEncryption
                            },
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            when (uiState.selectedTabIndex) {
                                0 -> stringResource(R.string.digital_wallet_add_document)
                                3 -> "Déposer PDF / Image"
                                else -> stringResource(R.string.digital_wallet_add_sensitive)
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("wallet_primary_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 700.dp)
        ) {
            if (uiState.isVaultLocked) {
                VaultLockedScreenContent(
                    hasCustomPin = uiState.hasCustomPin,
                    onBiometricUnlock = {
                        requestBiometricThen { walletViewModel.unlockVault() }
                    },
                    onPinUnlock = { showPinUnlockDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("digital_wallet_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isDeviceCompromised) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Alerte Intégrité : Appareil détecté comme rooté ou en mode débogage. Les secrets restent chiffrés via SQLCipher.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Status toast/banner feedback
                    uiState.statusMessage?.let { msg ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF065F46).copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(msg, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { walletViewModel.clearStatusMessage() }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Hero SQLCipher Vault Banner
                    item {
                        SqlCipherHeroVaultBanner(
                            documentsCount = documents.size,
                            sensitiveCount = sensitiveItems.size,
                            telemetry = uiState.telemetry,
                            onAddDocument = {
                                editingDocument = null
                                showDocumentDialog = true
                            },
                            onAddSensitive = {
                                editingSensitive = null
                                showSensitiveDialog = true
                            },
                            onScanOcr = onNavigateToDocumentScanner,
                            onImportProfile = {
                                walletViewModel.importCredentialsFromProfile(
                                    profile = cachedProfile,
                                    fallbackFullName = profileFullName ?: "",
                                    fallbackPassport = profilePassport ?: "",
                                    fallbackLicense = profileLicense ?: "",
                                    fallbackDocType = profileDocType ?: "",
                                    fallbackDocNumber = profileDocNumber ?: "",
                                    fallbackCountry = profileCountry ?: "",
                                    fallbackExpiry = profileExpiry ?: "",
                                    scannedDocs = cachedScannedDocs
                                )
                            }
                        )
                    }

                    // Segmented Tab Control
                    item {
                        val tabs = listOf(
                            0 to Translations.get(language, "wallet_tab_docs"),
                            1 to Translations.get(language, "wallet_tab_secrets"),
                            3 to Translations.get(language, "wallet_tab_files"),
                            2 to Translations.get(language, "wallet_tab_audit")
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tabs.forEach { (idx, label) ->
                                    val selected = uiState.selectedTabIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                HapticHelper.performClick(context, haptic)
                                                walletViewModel.selectTab(idx)
                                            }
                                            .padding(horizontal = 4.dp, vertical = 10.dp)
                                            .testTag("wallet_tab_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.selectedTabIndex != 2) {
                        item {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { walletViewModel.updateSearchQuery(it) },
                                placeholder = { Text("Rechercher dans le coffre chiffré...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { walletViewModel.updateSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Effacer la recherche")
                                        }
                                    }
                                } else null,
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wallet_search_input")
                            )
                        }
                    }

                    when (uiState.selectedTabIndex) {
                        0 -> {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(WalletDocCategory.entries) { cat ->
                                        FilterChip(
                                            selected = uiState.selectedDocCategory == cat.code,
                                            onClick = { walletViewModel.selectDocCategoryFilter(cat.code) },
                                            label = { Text(cat.localizedLabel(language)) }
                                        )
                                    }
                                }
                            }

                            if (filteredDocuments.isEmpty()) {
                                item {
                                    WalletEmptyStateCard(
                                        icon = Icons.Default.Badge,
                                        title = stringResource(R.string.digital_wallet_empty_docs_title),
                                        description = stringResource(R.string.digital_wallet_empty_docs_desc),
                                        primaryCta = stringResource(R.string.digital_wallet_add_document),
                                        onPrimaryClick = {
                                            editingDocument = null
                                            showDocumentDialog = true
                                        }
                                    )
                                }
                            } else {
                                items(filteredDocuments, key = { it.id }) { doc ->
                                    val isRevealed = !uiState.isPrivacyMaskEnabled || (doc.id in uiState.revealedItemIds)
                                    EncryptedIdentityDocumentCard(
                                        document = doc,
                                        isRevealed = isRevealed,
                                        language = language,
                                        onToggleReveal = { walletViewModel.toggleItemReveal(doc.id) },
                                        onCopyNumber = {
                                            copySensitiveToClipboard(context, "Numéro de pièce", doc.documentNumber)
                                        },
                                        onToggleFavorite = { walletViewModel.toggleDocumentFavorite(doc) },
                                        onInspectPass = { inspectingDocument = doc },
                                        onEdit = {
                                            editingDocument = doc
                                            showDocumentDialog = true
                                        },
                                        onDelete = {
                                            walletViewModel.deleteDocument(doc.id)
                                            eventViewModel.logActivity("WALLET_DOC_DELETED", "Suppression d'une pièce du coffre SQLCipher")
                                        }
                                    )
                                }
                            }
                        }

                        1 -> {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(SensitiveInfoCategory.entries) { cat ->
                                        FilterChip(
                                            selected = uiState.selectedInfoCategory == cat.code,
                                            onClick = { walletViewModel.selectInfoCategoryFilter(cat.code) },
                                            label = { Text(cat.localizedLabel(language)) }
                                        )
                                    }
                                }
                            }

                            if (filteredSensitiveItems.isEmpty()) {
                                item {
                                    WalletEmptyStateCard(
                                        icon = Icons.Default.LockPerson,
                                        title = stringResource(R.string.digital_wallet_empty_sensitive_title),
                                        description = stringResource(R.string.digital_wallet_empty_sensitive_desc),
                                        primaryCta = stringResource(R.string.digital_wallet_add_sensitive),
                                        onPrimaryClick = {
                                            editingSensitive = null
                                            showSensitiveDialog = true
                                        }
                                    )
                                }
                            } else {
                                items(filteredSensitiveItems, key = { it.id }) { item ->
                                    val isRevealed = !uiState.isPrivacyMaskEnabled || (item.id in uiState.revealedItemIds)
                                    EncryptedSensitiveInfoCard(
                                        item = item,
                                        isRevealed = isRevealed,
                                        language = language,
                                        onToggleReveal = {
                                            if (!isRevealed && item.requiresBiometricReveal) {
                                                requestBiometricThen { walletViewModel.toggleItemReveal(item.id) }
                                            } else {
                                                walletViewModel.toggleItemReveal(item.id)
                                            }
                                        },
                                        onCopySecret = {
                                            if (item.requiresBiometricReveal && !isRevealed) {
                                                requestBiometricThen {
                                                    copySensitiveToClipboard(context, item.title, item.secretValue)
                                                }
                                            } else {
                                                copySensitiveToClipboard(context, item.title, item.secretValue)
                                            }
                                        },
                                        onTogglePin = { walletViewModel.toggleSensitivePinned(item) },
                                        onEdit = {
                                            editingSensitive = item
                                            showSensitiveDialog = true
                                        },
                                        onDelete = {
                                            walletViewModel.deleteSensitiveInfo(item.id)
                                            eventViewModel.logActivity("WALLET_SECRET_DELETED", "Suppression d'un secret SQLCipher")
                                        }
                                    )
                                }
                            }
                        }

                        2 -> {
                            item {
                                SqlCipherAuditPanel(
                                    telemetry = uiState.telemetry,
                                    isScreenCaptureBlocked = uiState.isScreenCaptureBlocked,
                                    hasCustomPin = uiState.hasCustomPin,
                                    isRotatingKey = uiState.isProcessingKeyRotation,
                                    onToggleScreenCapture = { walletViewModel.toggleScreenCaptureProtection(it) },
                                    onConfigurePin = { showPinSetupDialog = true },
                                    onRotateKey = {
                                        requestBiometricThen {
                                            walletViewModel.rotateSqlCipherKey()
                                            eventViewModel.logActivity("SQLCIPHER_REKEY", "Rotation de la clé maîtresse SQLCipher 256-bit")
                                        }
                                    },
                                    onPurgeVault = { showPurgeConfirmDialog = true }
                                )
                            }
                        }

                        3 -> {
                            item {
                                EncryptedDocumentUploadHeaderCard(
                                    eccFingerprint = uiState.eccFingerprint,
                                    fileCount = encryptedFiles.size,
                                    isUploading = uiState.isUploadingEncryptedDoc,
                                    onPickPdfOrFile = {
                                        pdfAndDocPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                                    },
                                    onPickImagePhoto = {
                                        photoIdPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }

                            val matchingFiles = encryptedFiles.filter { file ->
                                val q = uiState.searchQuery.trim()
                                q.isEmpty() ||
                                    file.title.contains(q, ignoreCase = true) ||
                                    file.fileName.contains(q, ignoreCase = true)
                            }

                            if (matchingFiles.isEmpty()) {
                                item {
                                    WalletEmptyStateCard(
                                        icon = Icons.Default.UploadFile,
                                        title = "Aucun fichier PDF ou image chiffré",
                                        description = "Sélectionnez un justificatif PDF ou une photo d'identité. Il sera chiffré localement avec ECC P-256 + AES-256-GCM avant d'être enregistré dans SQLCipher et Firebase Storage.",
                                        primaryCta = "Chiffrer un PDF / Image",
                                        onPrimaryClick = {
                                            pdfAndDocPickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                                        }
                                    )
                                }
                            } else {
                                items(matchingFiles, key = { it.id }) { file ->
                                    EncryptedVaultFileCard(
                                        file = file,
                                        language = language,
                                        onDecryptAndOpen = {
                                            requestBiometricThen {
                                                walletViewModel.openEncryptedDocumentPreview(file)
                                            }
                                        },
                                        onRetryCloudSync = {
                                            walletViewModel.retryEncryptedFileCloudSync(file)
                                        },
                                        onDelete = {
                                            walletViewModel.deleteEncryptedDocument(file)
                                            eventViewModel.logActivity("ENCRYPTED_FILE_DELETED", "Suppression d'un document chiffré ECC (${file.title})")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDocumentDialog) {
        WalletDocumentFormDialog(
            initialDocument = editingDocument,
            defaultHolderName = cachedProfile?.fullName?.takeIf { it.isNotBlank() } ?: (profileFullName ?: ""),
            defaultCountry = cachedProfile?.residency?.takeIf { it.isNotBlank() } ?: (profileCountry ?: "France"),
            language = language,
            onDismiss = {
                showDocumentDialog = false
                editingDocument = null
            },
            onSave = { id, title, cat, num, holder, country, auth, issue, expiry, mrz, b64, notes, fav ->
                walletViewModel.saveIdentityDocument(
                    existingId = id,
                    title = title,
                    category = cat,
                    documentNumber = num,
                    holderFullName = holder,
                    issuingCountry = country,
                    issuingAuthority = auth,
                    issueDate = issue,
                    expiryDate = expiry,
                    mrzCode = mrz,
                    attachmentBase64 = b64,
                    notes = notes,
                    isFavorite = fav
                )
                eventViewModel.logActivity("WALLET_DOC_SAVED", "Enregistrement chiffré SQLCipher : $title")
                showDocumentDialog = false
                editingDocument = null
            }
        )
    }

    if (showSensitiveDialog) {
        SensitiveInfoFormDialog(
            initialItem = editingSensitive,
            language = language,
            onDismiss = {
                showSensitiveDialog = false
                editingSensitive = null
            },
            onSave = { id, title, cat, pLabel, secret, sLabel, sVal, notes, pinned, bio ->
                walletViewModel.saveSensitiveInfo(
                    existingId = id,
                    title = title,
                    category = cat,
                    primaryLabel = pLabel,
                    secretValue = secret,
                    secondaryLabel = sLabel,
                    secondaryValue = sVal,
                    confidentialNotes = notes,
                    isPinned = pinned,
                    requiresBiometricReveal = bio
                )
                eventViewModel.logActivity("WALLET_SECRET_SAVED", "Information sensible chiffrée SQLCipher : $title")
                showSensitiveDialog = false
                editingSensitive = null
            }
        )
    }

    inspectingDocument?.let { doc ->
        WalletDocumentDetailDialog(
            document = doc,
            onDismiss = { inspectingDocument = null },
            onEdit = {
                inspectingDocument = null
                editingDocument = doc
                showDocumentDialog = true
            }
        )
    }

    if (showPinSetupDialog) {
        VaultPinDialog(
            isSettingNewPin = true,
            onDismiss = { showPinSetupDialog = false },
            onSubmitPin = { pin ->
                walletViewModel.saveCustomPin(pin)
                showPinSetupDialog = false
                true
            }
        )
    }

    if (showPinUnlockDialog) {
        VaultPinDialog(
            isSettingNewPin = false,
            onDismiss = { showPinUnlockDialog = false },
            onSubmitPin = { pin ->
                val ok = walletViewModel.unlockWithPin(pin)
                if (ok) showPinUnlockDialog = false
                ok
            }
        )
    }

    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirmer la Purge Cryptographique") },
            text = {
                Text("Toutes les pièces d'identité et informations sensibles stockées dans la base SQLCipher seront définitivement effacées. Cette opération est irréversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurgeConfirmDialog = false
                        walletViewModel.purgeEntireVault()
                        eventViewModel.logActivity("SQLCIPHER_VAULT_PURGED", "Purge complète du coffre numérique SQLCipher")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Effacer définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    pendingUploadUri?.let { chosenUri ->
        EncryptedUploadStagingDialog(
            initialTitle = pendingUploadTitle,
            language = language,
            onDismiss = { pendingUploadUri = null },
            onConfirmUpload = { finalTitle, catCode, syncToCloud ->
                pendingUploadUri = null
                walletViewModel.uploadEncryptedDocument(
                    uri = chosenUri,
                    title = finalTitle,
                    docCategory = catCode,
                    uploadToCloud = syncToCloud
                ) { uploaded ->
                    if (uploaded != null) {
                        eventViewModel.logActivity(
                            "ENCRYPTED_DOC_UPLOADED",
                            "Document chiffré ECC P-256 : ${uploaded.title} (${uploaded.syncStatus})"
                        )
                    }
                }
            }
        )
    }

    EncryptedDocumentPreviewDialog(
        previewState = previewState,
        onDismiss = { walletViewModel.dismissDocumentPreview() }
    )
}

@Composable
private fun VaultLockedScreenContent(
    hasCustomPin: Boolean,
    onBiometricUnlock: () -> Unit,
    onPinUnlock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
            .testTag("wallet_locked_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(92.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Coffre verrouillé",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.digital_wallet_locked_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.digital_wallet_locked_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onBiometricUnlock,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .testTag("wallet_biometric_unlock_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.digital_wallet_unlock_button), fontWeight = FontWeight.Bold)
        }
        AnimatedVisibility(visible = hasCustomPin) {
            TextButton(
                onClick = onPinUnlock,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag("wallet_pin_unlock_button")
            ) {
                Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Déverrouiller avec mon Code PIN")
            }
        }
    }
}

@Composable
private fun WalletEmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    primaryCta: String,
    onPrimaryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_empty_state"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("wallet_empty_cta_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(primaryCta)
            }
        }
    }
}

private fun copySensitiveToClipboard(context: Context, label: String, secret: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText(label, secret)
    clip.description.extras = PersistableBundle().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        } else {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
        }
    }
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copié de manière confidentielle dans le presse-papier.", Toast.LENGTH_SHORT).show()
}
