package com.example.ui.screens
import androidx.compose.material.icons.filled.Storage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodels.EventViewModel
import com.example.ui.locales.Translations

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
) {
    val language by viewModel.language.collectAsState()
    val prayerNotifications by viewModel.prayerNotifications.collectAsState()
    val prayerCalculationMethod by viewModel.prayerCalculationMethod.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()
    val biometricLockEnabled by viewModel.biometricLockEnabled.collectAsState()
    val screenSecurityEnabled by viewModel.screenSecurityEnabled.collectAsState()
    val autoLockTimeout by viewModel.autoLockTimeout.collectAsState()
    val securityAuditLogs by viewModel.securityAuditLogs.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsState()
    val lastBackgroundSyncTime by viewModel.lastBackgroundSyncTime.collectAsState()
    val isBackgroundSyncEnabled by viewModel.isBackgroundSyncEnabled.collectAsState()
    val isRealtimeSyncActive by viewModel.isRealtimeSyncActive.collectAsState()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get(language, "settings_title")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Translations.get(language, "cancel"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Section
            Text(
                text = Translations.get(language, "personal_info"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { onNavigateToEditProfile() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(Translations.get(language, "nav_edit_profile"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Translations.get(language, "edit_profile_desc"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Notifications Section
            Text(
                text = Translations.get(language, "notifications"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val context = androidx.compose.ui.platform.LocalContext.current
            var methodExpanded by remember { mutableStateOf(false) }
            val calculationMethods = remember {
                listOf(
                    12 to "UOIF - France (12°)",
                    2 to "ISNA - Amérique du Nord",
                    3 to "Ligue Islamique Mondiale (MWL)",
                    4 to "Oumm al-Qura (La Mecque)",
                    5 to "Autorité Égyptienne",
                    1 to "Karachi (Univ. Sciences Islamiques)",
                    13 to "Diyanet (Turquie)"
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(Translations.get(language, "prayer_times"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(Translations.get(language, "receive_alerts"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = prayerNotifications,
                            onCheckedChange = { enabled ->
                                viewModel.updatePrayerNotifications(enabled)
                                if (!enabled) {
                                    com.example.notifications.PrayerNotificationScheduler.cancelAllPrayerNotifications(context)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Méthode de calcul des heures",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { methodExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val selectedName = calculationMethods.find { it.first == prayerCalculationMethod }?.second ?: "UOIF - France (12°)"
                            Text(selectedName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sélectionner méthode")
                        }

                        DropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            calculationMethods.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            name,
                                            fontWeight = if (id == prayerCalculationMethod) FontWeight.Bold else FontWeight.Normal,
                                            color = if (id == prayerCalculationMethod) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.updatePrayerCalculationMethod(id)
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & Confidentiality Section
            Text(
                text = Translations.get(language, "privacy"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Privacy Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(Translations.get(language, "privacy_mode"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(Translations.get(language, "privacy_mode_desc"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = privacyMode,
                            onCheckedChange = { viewModel.updatePrivacyMode(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Biometric Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Verrouillage Biométrique / PIN", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Sécurise l'accès aux justificatifs et pièces d'identité", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = biometricLockEnabled,
                            onCheckedChange = { viewModel.updateBiometricLock(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Anti-Screenshot Screen Security Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhonelinkLock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Protection Anti-Capture d'Écran", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Empêche les captures d'écran des données sensibles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = screenSecurityEnabled,
                            onCheckedChange = { viewModel.updateScreenSecurity(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Auto Lock Timeout Dropdown
                    var timeoutExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Délai d'Auto-Verrouillage", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Vérouille l'application après inactivité", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box {
                            OutlinedButton(onClick = { timeoutExpanded = true }) {
                                Text(autoLockTimeout)
                            }
                            DropdownMenu(
                                expanded = timeoutExpanded,
                                onDismissRequest = { timeoutExpanded = false }
                            ) {
                                listOf("Immédiat", "1 min", "5 min", "15 min", "Jamais").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.updateAutoLockTimeout(option)
                                            timeoutExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Background Real-Time Identity Sync Dashboard
            com.example.ui.components.IdentitySyncDashboard(
                isSyncEnabled = isBackgroundSyncEnabled,
                isRealtimeActive = isRealtimeSyncActive,
                lastSyncTimestamp = lastBackgroundSyncTime,
                syncStatusMessage = syncStatusMessage,
                onToggleSyncEnabled = { viewModel.setBackgroundSyncEnabled(it) },
                onTriggerSync = { viewModel.triggerBackgroundSyncNow() },
                onCheckConflicts = { viewModel.checkSyncConflicts() },
                onClearSyncMessage = { viewModel.clearSyncStatusMessage() },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Automated Secure Local JSON Backup for Personal Records
            com.example.ui.components.SecureLocalBackupDashboard(
                viewModel = viewModel,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Room <-> Firestore Sync & Cloud Backup Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sauvegarde Cloud & Synchronisation Room", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Chiffrez et sauvegardez l'état complet de votre base de données locale Room sur Firestore sous forme de blob JSON sécurisé.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.backupRoomDatabaseToCloud() },
                            enabled = !isBackingUp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isBackingUp) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sauvegarder", style = MaterialTheme.typography.bodyMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.restoreRoomDatabaseFromCloud() },
                            enabled = !isBackingUp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restauration", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.checkSyncConflicts() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vérifier Sync / Résoudre Conflits")
                    }

                    backupStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearBackupStatusMessage() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer message",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data Protection Shield & Audit Section
            Text(
                text = "Sécurité des Données & Chiffrement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            var showAuditDialog by remember { mutableStateOf(false) }
            var showPurgeConfirmDialog by remember { mutableStateOf(false) }
            var isPurging by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Chiffrement Client Android KeyStore (AES-256-GCM)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Les données d'identité et pièces justificatives dans la base de données Room sont chiffrées localement à l'aide d'une clé matérielle sécurisée.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAuditDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Journal d'Audit", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showPurgeConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Sécurisée", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (showAuditDialog) {
                AlertDialog(
                    onDismissRequest = { showAuditDialog = false },
                    title = { Text("Journal de Sécurité") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (securityAuditLogs.isEmpty()) {
                                Text("Aucun événement de sécurité enregistré.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                securityAuditLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAuditDialog = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }

            if (showPurgeConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showPurgeConfirmDialog = false },
                    title = { Text("Purge du Cache Sécurisé") },
                    text = { Text("Êtes-vous sûr de vouloir purger tous les justificatifs et pièces d'identité enregistrés dans le cache local ? Vos données en ligne restent intactes.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isPurging = true
                                viewModel.clearSensitiveDataCache {
                                    isPurging = false
                                    showPurgeConfirmDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            if (isPurging) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError)
                            } else {
                                Text("Purger maintenant")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPurgeConfirmDialog = false }) {
                            Text(Translations.get(language, "cancel"))
                        }
                    }
                )
            }

            // MFA & Authentication Section
            Text(
                text = Translations.get(language, "security_mfa"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            var showMfaEnrollment by remember { mutableStateOf(false) }
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val user = auth.currentUser
            val isMfaEnrolled = remember(user) { user?.multiFactor?.enrolledFactors?.isNotEmpty() == true }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(Translations.get(language, "mfa_enrollment"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(if (isMfaEnrolled) Translations.get(language, "enabled") else Translations.get(language, "disabled"), style = MaterialTheme.typography.bodyMedium, color = if (isMfaEnrolled) androidx.compose.ui.graphics.Color(0xFF10B981) else MaterialTheme.colorScheme.error)
                        }
                    }
                    Button(onClick = { showMfaEnrollment = true }, enabled = !isMfaEnrolled && user != null) {
                        Text(if (isMfaEnrolled) Translations.get(language, "configured") else Translations.get(language, "enable"))
                    }
                }
            }

            if (showMfaEnrollment && user != null) {
                var phoneNumber by remember { mutableStateOf("") }
                var mfaVerificationId by remember { mutableStateOf<String?>(null) }
                var mfaCode by remember { mutableStateOf("") }
                var mfaError by remember { mutableStateOf<String?>(null) }
                val context = androidx.compose.ui.platform.LocalContext.current

                AlertDialog(
                    onDismissRequest = { showMfaEnrollment = false },
                    title = { Text(Translations.get(language, "mfa_setup_title")) },
                    text = {
                        Column {
                            if (mfaError != null) {
                                Text(mfaError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (mfaVerificationId == null) {
                                Text(Translations.get(language, "mfa_phone_prompt"))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text(Translations.get(language, "phone_number")) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(Translations.get(language, "mfa_code_prompt"))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = mfaCode,
                                    onValueChange = { mfaCode = it },
                                    label = { Text(Translations.get(language, "sms_code")) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (mfaVerificationId == null) {
                                if (phoneNumber.isNotBlank()) {
                                    mfaError = null
                                    user.multiFactor.session.addOnCompleteListener { sessionTask ->
                                        if (sessionTask.isSuccessful) {
                                            val session = sessionTask.result
                                            val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber(phoneNumber)
                                                .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                                                .setActivity(context as android.app.Activity)
                                                .setMultiFactorSession(session)
                                                .setCallbacks(object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {}
                                                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                                                        mfaError = e.localizedMessage
                                                    }
                                                    override fun onCodeSent(verificationId: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                                                        mfaVerificationId = verificationId
                                                    }
                                                })
                                                .build()
                                            com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options)
                                        } else {
                                            mfaError = sessionTask.exception?.localizedMessage
                                        }
                                    }
                                }
                            } else {
                                if (mfaCode.isNotBlank()) {
                                    val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(mfaVerificationId!!, mfaCode)
                                    val assertion = com.google.firebase.auth.PhoneMultiFactorGenerator.getAssertion(credential)
                                    user.multiFactor.enroll(assertion, "Phone").addOnCompleteListener { enrollTask ->
                                        if (enrollTask.isSuccessful) {
                                            showMfaEnrollment = false
                                            // Ideally we should force recomposition of the parent
                                        } else {
                                            mfaError = enrollTask.exception?.localizedMessage
                                        }
                                    }
                                }
                            }
                        }) {
                            Text(if (mfaVerificationId == null) Translations.get(language, "send_sms") else Translations.get(language, "validate"))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMfaEnrollment = false }) {
                            Text(Translations.get(language, "cancel"))
                        }
                    }
                )
            }

            // Theme Section
            Text(
                text = Translations.get(language, "card_theme"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val currentTheme by viewModel.darkTheme.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(Translations.get(language, "theme_choice"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeOption(
                            label = Translations.get(language, "theme_light"),
                            selected = currentTheme == "light",
                            onClick = { viewModel.updateDarkTheme("light") }
                        )
                        ThemeOption(
                            label = Translations.get(language, "theme_dark"),
                            selected = currentTheme == "dark",
                            onClick = { viewModel.updateDarkTheme("dark") }
                        )
                        ThemeOption(
                            label = Translations.get(language, "theme_system"),
                            selected = currentTheme == "system",
                            onClick = { viewModel.updateDarkTheme("system") }
                        )
                    }
                }
            }

            // Language Section
            Text(
                text = Translations.get(language, "language"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(Translations.get(language, "app_language"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LanguageOption(
                            label = "Français",
                            selected = language == "fr",
                            onClick = { viewModel.updateLanguage("fr") }
                        )
                        LanguageOption(
                            label = "English",
                            selected = language == "en",
                            onClick = { viewModel.updateLanguage("en") }
                        )
                        LanguageOption(
                            label = "العربية",
                            selected = language == "ar",
                            onClick = { viewModel.updateLanguage("ar") }
                        )
                        LanguageOption(
                            label = "Español",
                            selected = language == "es",
                            onClick = { viewModel.updateLanguage("es") }
                        )
                        LanguageOption(
                            label = "Bahasa",
                            selected = language == "id",
                            onClick = { viewModel.updateLanguage("id") }
                        )
                    }
                }
            }

            // Data & Storage Section
            var isClearingCache by remember { mutableStateOf(false) }
            Text(
                text = Translations.get(language, "data_storage") ?: "Data & Storage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable(enabled = !isClearingCache) {
                        isClearingCache = true
                        viewModel.clearCacheAndRefresh {
                            isClearingCache = false
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isClearingCache) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(androidx.compose.material.icons.Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(Translations.get(language, "clear_cache") ?: "Clear Cache & Refresh", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(Translations.get(language, "clear_cache_desc") ?: "Resync local data from Firebase", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Danger Zone
            var showDeleteDialog by remember { mutableStateOf(false) }
            Text(
                text = Translations.get(language, "danger_zone") ?: "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { showDeleteDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(Translations.get(language, "delete_account") ?: "Delete Account", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(Translations.get(language, "delete_account_desc") ?: "Permanently delete your data", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(Translations.get(language, "delete_account") ?: "Delete Account") },
                    text = { Text(Translations.get(language, "delete_account_confirm") ?: "Are you sure you want to permanently delete your account? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                user?.delete()?.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        com.example.network.ApiClient.getSessionManager().logout()
                                        onNavigateBack() // this will trigger a recompose and eventually bring the user to Auth Screen
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(Translations.get(language, "delete") ?: "Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(Translations.get(language, "cancel"))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val animatedBg by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "theme_option_bg"
    )
    val animatedBorder by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "theme_option_border"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = animatedBg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, animatedBorder),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 2.dp, end = 6.dp)
            )
        }
    }
}
