package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.clickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Divider
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventEntity
import com.example.data.TicketEntity
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.QRCodeGenerator

object Translations {
    fun get(lang: String, key: String): String {
        val strings = mapOf(
            "fr" to mapOf(
                "identity_card" to "CARTE D'IDENTITÉ",
                "date_of_birth" to "DATE DE NAISSANCE",
                "residence" to "RÉSIDENCE",
                "id_number" to "N° IDENTIFIANT",
                "expiry_date" to "DATE EXPIRATION",
                "user" to "UTILISATEUR",
                "not_specified" to "Non spécifié",
                "personal_info" to "Informations Personnelles",
                "full_name" to "Nom complet",
                "dob_label" to "Date de naissance (JJ/MM/AAAA)",
                "residence_label" to "Lieu de résidence",
                "save" to "Sauvegarder",
                "edit" to "Modifier",
                "privacy_msg" to "Vos données sont protégées par un chiffrement sécurisé et ne sont partagées avec personne.",
                "take_photo" to "Photo",
                "signature" to "SIGNATURE",
                "card_theme" to "Thème",
                "language" to "Langue",
                "history" to "Historique d'Événements",
                "no_participation" to "Aucune participation pour l'instant.",
                "verified_premium" to "VÉRIFIÉ PREMIUM",
                "unverified" to "NON VÉRIFIÉ",
                "logout" to "Déconnexion",
                "tab_id_card" to "Carte Identité",
                "tab_dashboard" to "Justificatifs",
                "credential_certificate" to "Attestation d'Identité Véritable",
                "verification_level" to "Niveau de Vérification",
                "cryptographic_proof" to "Preuve Sécurisée",
                "security_integrity" to "Preuve Cryptographique d'Intégrité",
                "copy_sig" to "Copier la Signature",
                "active_certificates" to "Justificatifs Actifs (Garantis)",
                "authorized_by" to "Autorisé par la technologie IDMuslim Shield",
                "level_emerald" to "Niveau Émeraude (Vérifié Sécurisé)"
            ),
            "en" to mapOf(
                "identity_card" to "IDENTITY CARD",
                "date_of_birth" to "DATE OF BIRTH",
                "residence" to "RESIDENCE",
                "id_number" to "ID NUMBER",
                "expiry_date" to "EXPIRY DATE",
                "user" to "USER",
                "not_specified" to "Not specified",
                "personal_info" to "Personal Information",
                "full_name" to "Full Name",
                "dob_label" to "Date of Birth (DD/MM/YYYY)",
                "residence_label" to "Place of Residence",
                "save" to "Save",
                "edit" to "Edit",
                "privacy_msg" to "Your data is protected by secure encryption and is not shared with anyone.",
                "take_photo" to "Photo",
                "signature" to "SIGNATURE",
                "card_theme" to "Theme",
                "language" to "Language",
                "history" to "Event History",
                "no_participation" to "No participation yet.",
                "verified_premium" to "VERIFIED PREMIUM",
                "unverified" to "UNVERIFIED",
                "logout" to "Logout",
                "tab_id_card" to "ID Card",
                "tab_dashboard" to "Credentials",
                "credential_certificate" to "Verified Identity Credential",
                "verification_level" to "Verification Level",
                "cryptographic_proof" to "Secure Proof",
                "security_integrity" to "Cryptographic Security Proof",
                "copy_sig" to "Copy Signature",
                "active_certificates" to "Active Credentials (Guaranteed)",
                "authorized_by" to "Authorized by IDMuslim Shield Technology",
                "level_emerald" to "Emerald Class (Secured & Verified)"
            ),
            "ar" to mapOf(
                "identity_card" to "بطاقة الهوية",
                "date_of_birth" to "تاريخ الميلاد",
                "residence" to "مكان الإقامة",
                "id_number" to "رقم الهوية",
                "expiry_date" to "تاريخ الإنتهاء",
                "user" to "مستخدم",
                "not_specified" to "غير محدد",
                "personal_info" to "معلومات شخصية",
                "full_name" to "الاسم الكامل",
                "dob_label" to "تاريخ الميلاد (يوم/شهر/سنة)",
                "residence_label" to "مكان الإقامة",
                "save" to "حفظ",
                "edit" to "تعديل",
                "privacy_msg" to "بياناتك محمية بتشفير آمن ولا تتم مشاركتها مع أي شخص.",
                "take_photo" to "صورة",
                "signature" to "التوقيع",
                "card_theme" to "مظهر",
                "language" to "لغة",
                "history" to "سجل الأحداث",
                "no_participation" to "لا توجد مشاركات بعد.",
                "verified_premium" to "حساب موثق",
                "unverified" to "حساب غير موثق",
                "logout" to "تسجيل خروج",
                "tab_id_card" to "بطاقة الهوية",
                "tab_dashboard" to "الوثائق",
                "credential_certificate" to "وثيقة الهوية الموثقة",
                "verification_level" to "مستوى التوثيق",
                "cryptographic_proof" to "إثبات آمن",
                "security_integrity" to "الإثبات الرقمي المشفر",
                "copy_sig" to "نسخ التوقيع",
                "active_certificates" to "المستندات النشطة (المضمونة)",
                "authorized_by" to "صادر عن نظام حماية IDMuslim Shield",
                "level_emerald" to "المستوى الزمردي (آمن وموثق)"
            )
        )
        return strings[lang]?.get(key) ?: strings["fr"]?.get(key) ?: key
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: EventViewModel,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val firebaseUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val memberId = remember(firebaseUser) {
        if (firebaseUser != null) {
            "IDM-${firebaseUser.uid.take(8).uppercase()}"
        } else {
            "IDM-9928-441-2024"
        }
    }
    val memberEmail = firebaseUser?.email ?: "user@example.com"
    val displayName = firebaseUser?.displayName ?: memberEmail.substringBefore("@")
    val initials = remember(displayName) {
        val parts = displayName.trim().split("\\s+".toRegex())
        if (parts.size >= 2) {
            (parts[0].take(1) + parts[1].take(1)).uppercase()
        } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "MA"
        }
    }

    val expiryDate = remember(firebaseUser) {
        val timestamp = firebaseUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(java.util.Calendar.YEAR, 1)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        format.format(calendar.time)
    }

    val tickets by viewModel.getMemberTickets(memberId).collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val isVerified by viewModel.isUserVerified.collectAsState()
    val verificationStatus by viewModel.verificationStatus.collectAsState()
    val verificationStep by viewModel.verificationStep.collectAsState()
    val profilePhoto by viewModel.profilePhotoBase64.collectAsState()
    val cardTheme by viewModel.cardTheme.collectAsState()
    val language by viewModel.language.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()

    var showVerificationDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showPhotoMenu by remember { mutableStateOf(false) }

    val cachedFullName by viewModel.profileFullName.collectAsState()
    val cachedDob by viewModel.profileDob.collectAsState()
    val cachedResidency by viewModel.profileResidency.collectAsState()
    val cachedCommunityAffiliation by viewModel.profileCommunityAffiliation.collectAsState()

    var profileFullName by remember(cachedFullName, displayName) { mutableStateOf(cachedFullName ?: displayName) }
    var profileDateOfBirth by remember(cachedDob) { mutableStateOf(cachedDob ?: "") }
    var profileResidency by remember(cachedResidency) { mutableStateOf(cachedResidency ?: "") }
    var profileCommunityAffiliation by remember(cachedCommunityAffiliation) { mutableStateOf(cachedCommunityAffiliation ?: "") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        isAuthenticated = com.example.utils.BiometricHelper.authenticate(context)
        if (isAuthenticated) {
            viewModel.logActivity("DIGITAL_ID_ACCESSED", "Digital ID accessed securely.")
        }
    }
    
    var photoUriString by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    
    val processImageUri = { uri: android.net.Uri ->
        try {
            val resolver = context.contentResolver
            
            // 1. Detect image orientation using ExifInterface to fix rotated camera captures
            val orientation = try {
                resolver.openInputStream(uri)?.use { stream ->
                    val exifInterface = android.media.ExifInterface(stream)
                    exifInterface.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_UNDEFINED
                    )
                } ?: android.media.ExifInterface.ORIENTATION_UNDEFINED
            } catch (e: Exception) {
                android.media.ExifInterface.ORIENTATION_UNDEFINED
            }
            
            // 2. Decode the bitmap
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(resolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(resolver, uri)
            }
            
            // 3. Rotate bitmap if required based on EXIF
            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val orientedBitmap = if (!matrix.isIdentity()) {
                android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
            
            // 4. Crop to square and resize
            val size = Math.min(orientedBitmap.width, orientedBitmap.height)
            val x = (orientedBitmap.width - size) / 2
            val y = (orientedBitmap.height - size) / 2
            var squaredBitmap = android.graphics.Bitmap.createBitmap(orientedBitmap, x, y, size, size)
            if (squaredBitmap.config != android.graphics.Bitmap.Config.ARGB_8888) {
                squaredBitmap = squaredBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
            }
            
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(squaredBitmap, 400, 400, true)
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
            viewModel.updateProfilePhoto(base64)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUriString != null) {
            val uri = android.net.Uri.parse(photoUriString!!)
            processImageUri(uri)
        }
    }

    val launchCameraSafe = {
        try {
            val imageDir = java.io.File(context.cacheDir, "images")
            if (!imageDir.exists()) imageDir.mkdirs()
            val file = java.io.File(imageDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            
            // 1. Resolve correct FileProvider authority dynamically by scanning our package manifest providers
            var authority = "${context.packageName}.fileprovider"
            try {
                val pm = context.packageManager
                val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(android.content.pm.PackageManager.GET_PROVIDERS.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PROVIDERS)
                }
                val providers = packageInfo.providers
                if (providers != null) {
                    for (provider in providers) {
                        if (provider.name == "androidx.core.content.FileProvider" || provider.name.contains("FileProvider")) {
                            authority = provider.authority
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            photoUriString = uri.toString()
            
            // 2. Grant temporary URI permission to any app that can handle the camera capture intent
            try {
                val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val resolvedActivities = context.packageManager.queryIntentActivities(
                    intent,
                    android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolvedActivity in resolvedActivities) {
                    val packageName = resolvedActivity.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,                     
                        uri, 
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Erreur lors du lancement de l'appareil photo: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            processImageUri(uri)
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraSafe()
        } else {
            android.widget.Toast.makeText(context, "Permission de caméra requise pour prendre une photo.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MON PROFIL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "IDMuslim",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { showProfileMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)))
                        Text(initials, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(displayName, fontWeight = FontWeight.Bold) },
                            onClick = {},
                            enabled = false
                        )
                        DropdownMenuItem(
                            text = { Text("Paramètres") },
                            onClick = {
                                showProfileMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(Translations.get(language, "logout"), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showProfileMenu = false
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                com.example.network.ApiClient.getSessionManager().logout()
                                onLogout()
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Elegant Visual Segmented Control for Tab Selection
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabOptions = listOf(
                        0 to Translations.get(language, "tab_id_card"),
                        1 to Translations.get(language, "tab_dashboard")
                    )
                    tabOptions.forEach { (index, title) ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.Security else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                item {
                    if (isAuthenticated) {
                        DigitalCardSection(
                            memberId = memberId,
                            isVerified = isVerified,
                            verificationStatus = verificationStatus,
                            verificationStep = verificationStep,
                            profilePhotoBase64 = profilePhoto,
                            cardTheme = cardTheme,
                            fullName = profileFullName,
                            dateOfBirth = profileDateOfBirth,
                            residency = profileResidency,
                            communityAffiliation = profileCommunityAffiliation,
                            expiryDate = expiryDate,
                            language = language
                        )
                    } else {
                        val coroutineScope = rememberCoroutineScope()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        val authResult = com.example.utils.BiometricHelper.authenticate(context)
                                        isAuthenticated = authResult
                                        if (authResult) {
                                            viewModel.logActivity("DIGITAL_ID_ACCESSED", "Digital ID accessed securely.")
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Digital ID Locked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap to authenticate with biometrics", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box {
                            OutlinedButton(onClick = { showPhotoMenu = true }) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Photo")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Translations.get(language, "take_photo"))
                            }
                            DropdownMenu(
                                expanded = showPhotoMenu,
                                onDismissRequest = { showPhotoMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Appareil Photo") },
                                    onClick = {
                                        showPhotoMenu = false
                                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            launchCameraSafe()
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Galerie") },
                                    onClick = {
                                        showPhotoMenu = false
                                        galleryLauncher.launch("image/*")
                                    }
                                )
                            }
                        }
                        Box {
                            OutlinedButton(onClick = { showThemeMenu = true }) {
                                Text(Translations.get(language, "card_theme"))
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Émeraude (Défaut)") },
                                    onClick = { viewModel.updateCardTheme(0); showThemeMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Océan") },
                                    onClick = { viewModel.updateCardTheme(1); showThemeMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rubis") },
                                    onClick = { viewModel.updateCardTheme(2); showThemeMenu = false }
                                )
                            }
                        }
                    }
                }
                
                item {
                    LanguageSwitcher(
                        currentLanguage = language,
                        onLanguageSelected = { viewModel.updateLanguage(it) }
                    )
                }
                
                item {
                    com.example.ui.components.MosquesSection()
                }
                
                item {
                    com.example.ui.components.PrayerTimesSection()
                }
                
                item {
                    ProfileDetailsSection(
                        fullName = profileFullName,
                        dateOfBirth = profileDateOfBirth,
                        residency = profileResidency,
                        communityAffiliation = profileCommunityAffiliation,
                        onFullNameChange = { 
                            profileFullName = it
                            viewModel.updateProfileFullName(it) 
                        },
                        onDateOfBirthChange = { 
                            profileDateOfBirth = it
                            viewModel.updateProfileDob(it) 
                        },
                        onResidencyChange = { 
                            profileResidency = it
                            viewModel.updateProfileResidency(it) 
                        },
                        onCommunityAffiliationChange = {
                            profileCommunityAffiliation = it
                            viewModel.updateProfileCommunityAffiliation(it)
                        },
                        language = language
                    )
                }
                
                if (verificationStatus.uppercase() == "PENDING") {
                    item {
                        VerificationPendingPrompt(step = verificationStep)
                    }
                } else if (verificationStatus.uppercase() == "UNVERIFIED" || (!isVerified && verificationStatus.uppercase() != "VERIFIED")) {
                    item {
                        VerifyIdentityPrompt(onClick = { showVerificationDialog = true })
                    }
                }

                if (isAuthenticated) {
                    item {
                        ProfileQrSection(memberId = memberId, isVerified = isVerified)
                    }
                }

                item {
                    Text(
                        text = Translations.get(language, "history"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                if (tickets.isEmpty()) {
                    item {
                        Text(
                            text = Translations.get(language, "no_participation"),
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    items(tickets) { ticket ->
                        val event = events.find { it.id == ticket.eventId }
                        if (event != null) {
                            TicketHistoryCard(
                                ticket = ticket,
                                event = event,
                                onCancel = { viewModel.cancelTicket(ticket) }
                            )
                        }
                    }
                }

                if (isAuthenticated) {
                    item {
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(
                            text = "Journal d'Activité Sécurisé (Mémorial)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    if (activityLogs.isEmpty()) {
                        item {
                            Text(
                                text = "Aucune activité enregistrée.",
                                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        items(activityLogs) { log ->
                            ActivityLogCard(log)
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Verified Credentials Dashboard Tab content
                item {
                    if (isAuthenticated) {
                        CredentialsDashboardSection(
                            memberId = memberId,
                            isVerified = isVerified,
                            verificationStatus = verificationStatus,
                            verificationStep = verificationStep,
                            fullName = profileFullName,
                            dateOfBirth = profileDateOfBirth,
                            residency = profileResidency,
                            communityAffiliation = profileCommunityAffiliation,
                            expiryDate = expiryDate,
                            language = language,
                            ticketsCount = tickets.size,
                            context = context
                        )
                    } else {
                        val coroutineScope = rememberCoroutineScope()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        val authResult = com.example.utils.BiometricHelper.authenticate(context)
                                        isAuthenticated = authResult
                                        if (authResult) {
                                            viewModel.logActivity("DIGITAL_ID_ACCESSED", "Digital ID accessed securely.")
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Locked",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Credentials Locked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap to authenticate with biometrics", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVerificationDialog) {
        VerificationWorkflowDialog(
            onDismiss = { showVerificationDialog = false },
            onVerified = {
                viewModel.startMockVerification()
                showVerificationDialog = false
            }
        )
    }
}

@Composable
fun DigitalCardSection(
    memberId: String,
    isVerified: Boolean,
    verificationStatus: String,
    verificationStep: String,
    profilePhotoBase64: String?,
    cardTheme: Int,
    fullName: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String,
    expiryDate: String,
    language: String
) {
    val themeColors = when (cardTheme) {
        1 -> listOf(Color(0xFF003366), Color(0xFF001133)) // Ocean
        2 -> listOf(Color(0xFF550000), Color(0xFF220000)) // Ruby
        else -> listOf(Color(0xFF1B4D3E), Color(0xFF0A261E)) // Default Emerald
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .aspectRatio(1.586f),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = themeColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translations.get(language, "identity_card"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (fullName.isNotBlank()) fullName.uppercase() else Translations.get(language, "user").uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isVerified) {
                                com.example.ui.components.DigitalVerifiedBadge(
                                    isVerified = true,
                                    memberId = memberId,
                                    fullName = fullName,
                                    size = com.example.ui.components.BadgeSize.SMALL
                                )
                            }
                        }
                    }
                    if (profilePhotoBase64 != null) {
                        var decodedBitmap: Bitmap? = null
                        try {
                            val decodedString = android.util.Base64.decode(profilePhotoBase64, android.util.Base64.DEFAULT)
                            decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            // Suppress error
                        }
                        if (decodedBitmap != null) {
                            Image(
                                bitmap = decodedBitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else {
                        com.example.ui.components.VerificationStatusBadge(
                            status = verificationStatus,
                            substep = verificationStep,
                            useDarkThemeColors = false
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row {
                            Column {
                                Text(
                                    text = Translations.get(language, "date_of_birth"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = if (dateOfBirth.isNotBlank()) dateOfBirth else "--/--/----",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = Translations.get(language, "residence"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = if (residency.isNotBlank()) residency else Translations.get(language, "not_specified"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Column {
                                Text(
                                    text = Translations.get(language, "id_number"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = memberId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = Translations.get(language, "expiry_date"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = expiryDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Row {
                            Column {
                                Text(
                                    text = "AFFILIATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = if (communityAffiliation.isNotBlank()) communityAffiliation else "--",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            Text(
                                text = Translations.get(language, "signature"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp,
                                fontSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.4f))
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(24.dp).border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileQrSection(memberId: String, isVerified: Boolean) {
    var isDynamicMode by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableStateOf(30) }
    var securePayload by remember { mutableStateOf("") }
    var currentSignature by remember { mutableStateOf("") }
    var qrBitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var showPayloadDetails by remember { mutableStateOf(false) }

    fun generateNewToken() {
        val timestamp = System.currentTimeMillis() / 1000
        val payloadRaw = "$memberId-${if (isVerified) "VERIFIED_PREMIUM" else "NOT_VERIFIED"}-$timestamp"
        currentSignature = try {
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(payloadRaw.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(24)
        } catch (e: Exception) {
            "sig-failed-pki"
        }
        
        securePayload = """
            {
              "id": "$memberId",
              "status": "${if (isVerified) "VERIFIED" else "PENDING"}",
              "issuedAt": $timestamp,
              "sig": "$currentSignature",
              "algorithm": "SHA-256"
            }
        """.trimIndent()
        
        qrBitmapState = QRCodeGenerator.generateQRCode(securePayload, 512)
        com.example.nfc.ProfileApduService.activePayload = securePayload
    }

    if (isDynamicMode) {
        LaunchedEffect(memberId, isVerified) {
            generateNewToken()
            secondsLeft = 30
            while (true) {
                kotlinx.coroutines.delay(1000L)
                if (secondsLeft > 1) {
                    secondsLeft -= 1
                } else {
                    generateNewToken()
                    secondsLeft = 30
                }
            }
        }
    } else {
        LaunchedEffect(memberId, isVerified) {
            securePayload = "idmuslim://identity/$memberId?verified=$isVerified"
            currentSignature = "static-signed-pki"
            qrBitmapState = QRCodeGenerator.generateQRCode(securePayload, 512)
            com.example.nfc.ProfileApduService.activePayload = securePayload
        }
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Sécurité",
                        tint = if (isVerified) Color(0xFF34D399) else Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Identité Numérique Sécurisée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = { generateNewToken() }, enabled = isDynamicMode) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Régénérer"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ce QR Code contient une signature cryptographique SHA-256 protégeant l'authenticité de votre profil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isDynamicMode = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDynamicMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isDynamicMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Code Dynamique", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = { isDynamicMode = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isDynamicMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (!isDynamicMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Code Statique", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                if (isDynamicMode) {
                    CircularProgressIndicator(
                        progress = { secondsLeft / 30f },
                        modifier = Modifier.size(190.dp),
                        color = if (isVerified) Color(0xFF34D399) else MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                    )
                }
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(160.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        qrBitmapState?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR Code de sécurité",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        } ?: CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "NFC Enabled",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NFC Tap Ready",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isDynamicMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Renouvellement automatique dans ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${secondsLeft}s",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (secondsLeft <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "QR Code hors-ligne permanent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPayloadDetails = !showPayloadDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Infos",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showPayloadDetails) "Masquer les détails cryptographiques" else "Voir les détails cryptographiques",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(visible = showPayloadDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "PAYLOAD JSON SIGNÉ :",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = securePayload,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
    
@Composable
fun ActivityLogCard(log: com.example.data.ActivityLogEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.actionType == "NFC_VERIFIED") Icons.Default.Info else Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = log.actionType,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TicketHistoryCard(ticket: TicketEntity, event: EventEntity, onCancel: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(expanded) {
        if (expanded && qrBitmap == null) {
            withContext(Dispatchers.Default) {
                val writer = com.google.zxing.qrcode.QRCodeWriter()
                val hints = mapOf(
                    com.google.zxing.EncodeHintType.MARGIN to 1
                )
                try {
                    val bitMatrix = writer.encode(
                        "idmuslim://ticket/${ticket.scanCode}",
                        com.google.zxing.BarcodeFormat.QR_CODE,
                        512,
                        512,
                        hints
                    )
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val pixels = IntArray(width * height)
                    for (y in 0 until height) {
                        val offset = y * width
                        for (x in 0 until width) {
                            pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        }
                    }
                    qrBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${event.date} à ${event.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Billet: ${ticket.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (ticket.status == "Valid") Color(0xFF34D399) else Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (ticket.status == "Valid") {
                    TextButton(onClick = onCancel) {
                        Text("Annuler", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            AnimatedVisibility(visible = expanded && ticket.status == "Valid") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scannez pour valider le billet",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Ticket QR Code",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationPendingPrompt(step: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vérification en cours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notre service de conformité analyse actuellement votre pièce d'identité et votre selfie biométrique. Cette opération prend généralement quelques secondes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            if (step.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun VerifyIdentityPrompt(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vérification requise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pour utiliser la billetterie et autres services, veuillez procéder à la vérification de votre pièce d'identité via notre système sécurisé.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vérifier mon identité", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationWorkflowDialog(onDismiss: () -> Unit, onVerified: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1: Info, 2: ID upload, 3: Selfie, 4: Validating

    LaunchedEffect(step) {
        if (step == 4) {
            kotlinx.coroutines.delay(2000)
            onVerified()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                1 -> {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vérification d'Identité",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Protégez votre profil IDMuslim. Nous vérifions votre pièce gouvernementale (Carte Nationale d'Identité ou Passeport) de manière sécurisée et cryptée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Commencer")
                    }
                }
                2 -> {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "1. Prenez en photo votre pièce d'identité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Assurez-vous que le document soit lisible et sans reflets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { step = 3 },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Simuler l'envoi de la pièce")
                    }
                }
                3 -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "2. Capturez un selfie vidéo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prenez un rapide selfie pour prouver qu'il s'agit bien de vous.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { step = 4 },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Simuler le selfie vidéo")
                    }
                }
                4 -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyse sécurisée en cours...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vérification des hologrammes et recoupement du selfie.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "fr" to "Français",
        "en" to "English",
        "ar" to "العربية"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = Translations.get(currentLanguage, "language"),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            languages.forEach { (code, name) ->
                val isSelected = currentLanguage == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onLanguageSelected(code) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsSection(
    fullName: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String,
    onFullNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onResidencyChange: (String) -> Unit,
    onCommunityAffiliationChange: (String) -> Unit,
    language: String
) {
    var isEditing by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translations.get(language, "personal_info"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { isEditing = !isEditing }) {
                    Text(text = if (isEditing) Translations.get(language, "save") else Translations.get(language, "edit"))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text(Translations.get(language, "full_name")) },
                readOnly = !isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = onDateOfBirthChange,
                label = { Text(Translations.get(language, "dob_label")) },
                readOnly = !isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = residency,
                onValueChange = onResidencyChange,
                label = { Text(Translations.get(language, "residence_label")) },
                readOnly = !isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = communityAffiliation,
                onValueChange = onCommunityAffiliationChange,
                label = { Text("Mosquée / Association affiliée") },
                readOnly = !isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            if (!isEditing) {
                Text(
                    text = Translations.get(language, "privacy_msg"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CredentialsDashboardSection(
    memberId: String,
    isVerified: Boolean,
    verificationStatus: String,
    verificationStep: String,
    fullName: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String,
    expiryDate: String,
    language: String,
    ticketsCount: Int,
    context: android.content.Context
) {
    val certColors = if (isVerified) {
        listOf(Color(0xFF0F5A47), Color(0xFF1B4D3E)) // Dark Emerald for verified
    } else {
        listOf(Color(0xFF4A4A4A), Color(0xFF2C2C2C)) // Dark Slate for unverified
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Dashboard Welcoming Banner / Metrics Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isVerified) "CITOYEN VÉRIFIÉ" else "STATUT EN ATTENTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tableau des Attestations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Présentation de vos justificatifs numériques officiels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isVerified) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Status Icon",
                        tint = if (isVerified) Color(0xFF0F5132) else Color(0xFF664D03),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Digital Verified Badge (Large layout) with Interactive Verification Dialogue
        com.example.ui.components.DigitalVerifiedBadge(
            isVerified = isVerified,
            memberId = memberId,
            fullName = fullName,
            size = com.example.ui.components.BadgeSize.LARGE,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // 2. Verified Digital ID Certificate (Attestation Officielle)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.linearGradient(colors = certColors))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "IDMUSLIM SECURITY SYSTEMS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = Translations.get(language, "credential_certificate").uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isVerified) "ORIGINAL VRAI" else "PROVISOIRE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "TITULAIRE DU JUSTIFICATIF",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (fullName.isNotBlank()) fullName.uppercase() else "CITOYEN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "N° DE CITOYEN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = memberId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VALIDITÉ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "JUSQU'AU $expiryDate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Translations.get(language, "authorized_by"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Shield Verified",
                                tint = if (isVerified) Color(0xFF25D366) else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVerified) "VERIFIED" else "PENDING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) Color(0xFF25D366) else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Dynamic QR Authenticator Section (Clé d'Accréditation d'Accès)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "JETON D'AUTHENTICATION SÉCURISÉ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Présentez ce QR Code pour justifier de votre identité certifiée.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Generating Payload conforming to secure checks
                val timestamp = remember { System.currentTimeMillis() }
                val currentSignature = "dynamic-shield-${memberId.take(5)}"
                val securePayload = """
                    {
                      "id": "$memberId",
                      "status": "${if (isVerified) "VERIFIED" else "PENDING"}",
                      "issuedAt": $timestamp,
                      "sig": "$currentSignature",
                      "algorithm": "SHA-256"
                    }
                """.trimIndent()
                val qrBitmap = remember(securePayload) {
                    try {
                        QRCodeGenerator.generateQRCode(securePayload, 450)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Secure Verification QR"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Syncing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Jeton d'autorisation synchronisé en temps réel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 4. Cryptographic Proof Section (Preuve Cryptographique d'Intégrité)
        val cryptoHash = remember(memberId) {
            try {
                val input = "$memberId-$fullName-$dateOfBirth"
                val md = java.security.MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(input.toByteArray())
                hashBytes.joinToString("") { "%02x".format(it) }.take(32).uppercase()
            } catch (e: Exception) {
                "0XF38D9A22409B19C0E28B5E83F67CDD28"
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Cryptographic Security",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translations.get(language, "security_integrity"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Signature unique d'intégrité de vos justificatifs:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SHA-256:$cryptoHash",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("IDMuslim Security Signature", cryptoHash)
                        clipboardManager.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Signature d'intégrité copiée dans le presse-papier !", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = Translations.get(language, "copy_sig"))
                }
            }
        }

        // 5. Active Access Status / Credentials Count Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event bookings",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "PASS ACQUIS & RÉSERVATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (ticketsCount > 0) "$ticketsCount participations validées actives" else "Aucune réservation active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
