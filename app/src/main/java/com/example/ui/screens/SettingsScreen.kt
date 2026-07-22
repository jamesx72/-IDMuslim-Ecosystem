package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
) {
    val language by viewModel.language.collectAsState()
    val prayerNotifications by viewModel.prayerNotifications.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(Translations.get(language, "prayer_times"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(Translations.get(language, "receive_alerts"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = prayerNotifications,
                        onCheckedChange = { viewModel.updatePrayerNotifications(it) }
                    )
                }
            }

            // Privacy Section
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
            }

            // Theme Section
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
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) // Used Settings icon as placeholder
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label, modifier = Modifier.padding(start = 4.dp))
    }
}
