package com.example.ui.screens
import androidx.compose.material.icons.filled.Storage
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import com.example.utils.HapticHelper
import com.example.ui.components.CardVisualThemes
import com.example.ui.components.IslamicCardPatternBackground

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
    val cardTheme by viewModel.cardTheme.collectAsState()
    val cardFontScale by viewModel.cardFontScale.collectAsState()
    val isSolarAdaptiveTheme by viewModel.isSolarAdaptiveTheme.collectAsState()
    val solarSimulationOverride by viewModel.solarSimulationOverride.collectAsState()
    val solarState by viewModel.solarState.collectAsState()
    val cachedUserProfile by viewModel.cachedUserProfile.collectAsState()
    val profileFullName by viewModel.profileFullName.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
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

            // ==========================================
            // DIGITAL ID CARD VISUAL THEME & ISLAMIC ART MOTIFS
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.get(language, "card_theme_customization_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = Translations.get(language, "card_theme_customization_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Interactive Live Mini Card Preview
            val effectivePreviewThemeId = if (isSolarAdaptiveTheme) solarState.adaptedThemeId else cardTheme
            val activeVisualTheme = remember(effectivePreviewThemeId) { CardVisualThemes.getThemeById(effectivePreviewThemeId) }
            val effectiveGradientColors = if (isSolarAdaptiveTheme) solarState.adaptedGradientColors else activeVisualTheme.gradientColors
            val effectiveAccentColor = if (isSolarAdaptiveTheme) solarState.adaptedAccentColor else activeVisualTheme.accentColor

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = effectiveGradientColors
                            )
                        )
                ) {
                    // Cultural Pattern Background Canvas
                    IslamicCardPatternBackground(
                        themeIndex = effectivePreviewThemeId,
                        modifier = Modifier.fillMaxSize(),
                        alphaMultiplier = 1.0f
                    )

                    // Card Content Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = effectiveAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IDMUSLIM CARD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Motif Badge Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = effectiveAccentColor.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, effectiveAccentColor.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSolarAdaptiveTheme) solarState.phaseIcon else activeVisualTheme.patternIcon,
                                        contentDescription = null,
                                        tint = effectiveAccentColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSolarAdaptiveTheme) solarState.phaseDisplayName.uppercase() else activeVisualTheme.badgeLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = effectiveAccentColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Middle: Shahada watermark
                        val previewShahadaSize = (11f * cardFontScale).coerceIn(9f, 13f).sp
                        Text(
                            text = Translations.get(language, "shahada_text"),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = previewShahadaSize),
                            color = Color.White.copy(alpha = 0.85f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bottom row
                        val previewDisplayName = profileFullName?.ifBlank { null } ?: cachedUserProfile?.fullName ?: Translations.get(language, "user")
                        val previewMemberId = cachedUserProfile?.idNumber?.ifBlank { null } ?: "IDM-7860-9942"
                        val previewNameSize = (14f * cardFontScale).coerceIn(12f, 17f).sp
                        val previewIdSize = (9f * cardFontScale).coerceIn(7.5f, 12f).sp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = previewDisplayName.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = previewNameSize),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = previewMemberId,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = previewIdSize),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = effectiveAccentColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isSolarAdaptiveTheme) "SOLAR ACTIVE" else "CHIP SECURED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Automated Solar-Adaptive Theme Switcher Horizon Control Card
            com.example.ui.components.SolarHorizonCard(
                solarState = solarState,
                isSolarAdaptiveEnabled = isSolarAdaptiveTheme,
                activeOverride = solarSimulationOverride,
                onToggleAdaptive = { enabled ->
                    viewModel.updateSolarAdaptiveTheme(enabled)
                },
                onSelectOverride = { phaseKey ->
                    viewModel.updateSolarSimulationOverride(phaseKey)
                },
                language = language,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Motif Theme Selection List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CardVisualThemes.themes.forEach { theme ->
                    val isSelected = cardTheme == theme.id
                    val themeName = Translations.get(language, theme.nameKey).ifBlank { theme.defaultName }
                    val themeDesc = Translations.get(language, theme.descKey).ifBlank { theme.defaultDesc }

                    Surface(
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            viewModel.updateCardTheme(theme.id)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, theme.accentColor)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Icon Box with gradient preview background
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.linearGradient(theme.gradientColors)
                                    )
                                    .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = theme.patternIcon,
                                    contentDescription = themeName,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Text details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = themeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.accentColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = theme.badgeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = theme.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = themeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Color swatch dots
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    theme.gradientColors.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Selection Indicator
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                 )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // ID CARD ACCESSIBILITY & FONT SCALE
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatSize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.get(language, "card_accessibility_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = Translations.get(language, "card_accessibility_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Live Percentage Badge & Reset button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${(cardFontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (kotlin.math.abs(cardFontScale - 1.0f) > 0.01f) {
                            TextButton(
                                onClick = {
                                    HapticHelper.performClick(context, haptic)
                                    viewModel.updateCardFontScale(1.0f)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Translations.get(language, "font_scale_reset"),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Typography Preview Bubble inside Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            val previewLabelSize = (8f * cardFontScale).coerceIn(7f, 11f).sp
                            val previewValSize = (13f * cardFontScale).coerceIn(10.5f, 17f).sp

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = Translations.get(language, "full_name").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = previewLabelSize,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = (profileFullName?.ifBlank { null } ?: "Zayd Ibn Ali").uppercase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = (15f * cardFontScale).coerceIn(12f, 18f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Translations.get(language, "id_number").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = previewLabelSize,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = cachedUserProfile?.idNumber?.ifBlank { null } ?: "IDM-7860-9942",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = previewValSize,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = Translations.get(language, "residence").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = previewLabelSize,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Paris, France",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = previewValSize,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Translations.get(language, "expiry_date").uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = previewLabelSize,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "31/12/2030",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = previewValSize,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Slider Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = cardFontScale,
                            onValueChange = { newValue ->
                                val rounded = (kotlin.math.round(newValue * 20f) / 20f).coerceIn(0.75f, 1.40f)
                                if (rounded != cardFontScale) {
                                    HapticHelper.performClick(context, haptic)
                                    viewModel.updateCardFontScale(rounded)
                                }
                            },
                            valueRange = 0.75f..1.40f,
                            steps = 12,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Buttons Grid
                    val presets = listOf(
                        0.85f to Translations.get(language, "font_scale_compact"),
                        1.00f to Translations.get(language, "font_scale_standard"),
                        1.15f to Translations.get(language, "font_scale_large"),
                        1.30f to Translations.get(language, "font_scale_extra_large")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (scale, label) ->
                            val isSelected = kotlin.math.abs(cardFontScale - scale) < 0.04f
                            Surface(
                                onClick = {
                                    HapticHelper.performClick(context, haptic)
                                    viewModel.updateCardFontScale(scale)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${(scale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // APP INTERFACE THEME (LIGHT / DARK / SYSTEM)
            // ==========================================
            Text(
                text = Translations.get(language, "theme_choice"),
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

                    // Interactive Crossfade Theme Preview
                    Crossfade(
                        targetState = currentTheme,
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "theme_card_crossfade_preview"
                    ) { theme ->
                        val previewBg = when (theme) {
                            "dark" -> androidx.compose.ui.graphics.Color(0xFF1E293B)
                            "light" -> androidx.compose.ui.graphics.Color(0xFFF1F5F9)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val previewContentColor = when (theme) {
                            "dark" -> androidx.compose.ui.graphics.Color(0xFFF8FAFC)
                            "light" -> androidx.compose.ui.graphics.Color(0xFF0F172A)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val previewLabel = when (theme) {
                            "dark" -> "Mode Sombre Actif"
                            "light" -> "Mode Clair Actif"
                            else -> "Mode Système (Adaptatif)"
                        }
                        val previewIcon = when (theme) {
                            "dark" -> Icons.Default.DarkMode
                            "light" -> Icons.Default.LightMode
                            else -> Icons.Default.BrightnessAuto
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = previewBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = previewIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = previewLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = previewContentColor
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeOption(
                            label = Translations.get(language, "theme_light"),
                            icon = Icons.Default.LightMode,
                            selected = currentTheme == "light",
                            onClick = { viewModel.updateDarkTheme("light") }
                        )
                        ThemeOption(
                            label = Translations.get(language, "theme_dark"),
                            icon = Icons.Default.DarkMode,
                            selected = currentTheme == "dark",
                            onClick = { viewModel.updateDarkTheme("dark") }
                        )
                        ThemeOption(
                            label = Translations.get(language, "theme_system"),
                            icon = Icons.Default.BrightnessAuto,
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
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
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
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
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
