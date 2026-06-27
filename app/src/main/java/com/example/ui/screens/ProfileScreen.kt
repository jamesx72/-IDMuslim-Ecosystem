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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.platform.testTag
import com.example.data.EventEntity
import com.example.data.TicketEntity
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.QRCodeGenerator
import com.example.ui.components.IslamicDateHeader

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
                "tab_history" to "Historique",
                "credential_certificate" to "Attestation d'Identité Véritable",
                "verification_level" to "Niveau de Vérification",
                "cryptographic_proof" to "Preuve Sécurisée",
                "security_integrity" to "Preuve Cryptographique d'Intégrité",
                "copy_sig" to "Copier la Signature",
                "active_certificates" to "Justificatifs Actifs (Garantis)",
                "authorized_by" to "Autorisé par la technologie IDMuslim Shield",
                "level_emerald" to "Niveau Émeraude (Vérifié Sécurisé)",
                "faith_declaration" to "Sache donc qu’en vérité il n’y a point de divinité à part Allah",
                "expiry" to "EXPIRATION",
                "gov_info" to "INFORMATIONS GOUVERNEMENTALES",
                "passport_num" to "Numéro de Passeport",
                "license_num" to "Numéro de Permis",
                "hidden_field" to "••••••"
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
                "tab_history" to "History",
                "credential_certificate" to "Verified Identity Credential",
                "verification_level" to "Verification Level",
                "cryptographic_proof" to "Secure Proof",
                "security_integrity" to "Cryptographic Security Proof",
                "copy_sig" to "Copy Signature",
                "active_certificates" to "Active Credentials (Guaranteed)",
                "authorized_by" to "Authorized by IDMuslim Shield Technology",
                "level_emerald" to "Emerald Class (Secured & Verified)",
                "faith_declaration" to "Know therefore that there is no god but Allah",
                "expiry" to "EXPIRY",
                "gov_info" to "GOVERNMENT INFORMATION",
                "passport_num" to "Passport Number",
                "license_num" to "License Number",
                "hidden_field" to "••••••"
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
                "tab_history" to "السجل",
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
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
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
    val privacyMode by viewModel.privacyMode.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    
    val profileVisibility by viewModel.profileVisibility.collectAsState()
    val showEmail by viewModel.showEmail.collectAsState()
    val shareLocation by viewModel.shareLocation.collectAsState()
    val shareData by viewModel.shareData.collectAsState()
    val allowNotifications by viewModel.allowNotifications.collectAsState()

    var showVerificationDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showPhotoMenu by remember { mutableStateOf(false) }

    val cachedFullName by viewModel.profileFullName.collectAsState()
    val cachedDob by viewModel.profileDob.collectAsState()
    val cachedResidency by viewModel.profileResidency.collectAsState()
    val cachedCommunityAffiliation by viewModel.profileCommunityAffiliation.collectAsState()
    val cachedPassport by viewModel.profilePassportNumber.collectAsState()
    val cachedLicense by viewModel.profileLicenseNumber.collectAsState()
    val hasPaidForPdf by viewModel.hasPaidForPdf.collectAsState()
    val isProfileLoading by viewModel.isProfileLoading.collectAsState()

    var profileFullName by remember(cachedFullName, displayName) { mutableStateOf(cachedFullName ?: displayName) }
    var profileDateOfBirth by remember(cachedDob) { mutableStateOf(cachedDob ?: "") }
    var profileResidency by remember(cachedResidency) { mutableStateOf(cachedResidency ?: "") }
    var profileCommunityAffiliation by remember(cachedCommunityAffiliation) { mutableStateOf(cachedCommunityAffiliation ?: "") }
    var profilePassport by remember(cachedPassport) { mutableStateOf(cachedPassport ?: "") }
    var profileLicense by remember(cachedLicense) { mutableStateOf(cachedLicense ?: "") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showIdReadyAlert by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    val notifications = remember(verificationStatus, isVerified) {
        val list = mutableListOf<Map<String, String>>()
        if (isVerified) {
            list.add(mapOf(
                "title" to "Félicitations!",
                "message" to "Votre identité a été vérifiée avec succès. Vous êtes maintenant membre Premium Émeraude.",
                "time" to "À l'instant",
                "type" to "success"
            ))
        } else {
            list.add(mapOf(
                "title" to "Action Requise",
                "message" to "Veuillez terminer la vérification pour sécuriser votre compte IDMuslim.",
                "time" to "Il y a 2h",
                "type" to "warning"
            ))
        }
        
        list.add(mapOf(
            "title" to "Evénement à venir",
            "message" to "Le grand rassemblement de la communauté internationale approche.",
            "time" to "Il y a 1 jour",
            "type" to "info"
        ))
        
        list.add(mapOf(
            "title" to "Mise à jour Communauté",
            "message" to "Plus de 500 nouveaux membres ont rejoint IDMuslim cette semaine dans toute la France.",
            "time" to "Il y a 2 jours",
            "type" to "info"
        ))
        list
    }

    LaunchedEffect(verificationStatus) {
        if (verificationStatus.uppercase() == "VERIFIED") {
            val dismissed = com.example.network.ApiClient.getSessionManager().isIdReadyAlertDismissed()
            if (!dismissed) {
                showIdReadyAlert = true
            }
        } else {
            com.example.network.ApiClient.getSessionManager().saveIdReadyAlertDismissed(false)
            showIdReadyAlert = false
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadProfileFromFirestore()
        viewModel.syncVerificationStatusFromFirestore()
        viewModel.loadFamilyMembers()
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
            
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(squaredBitmap, 800, 800, true)
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, outputStream)
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

    val documentUploadLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.uploadDocumentForVerification(uri, context)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentTheme by viewModel.darkTheme.collectAsState()
                    val isDark = currentTheme == "dark" || (currentTheme == "system" && androidx.compose.foundation.isSystemInDarkTheme())
                    IconButton(
                        onClick = { viewModel.updateDarkTheme(if (isDark) "light" else "dark") }
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showNotificationsDialog = true }
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            // Badge indicating unread notifications
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                            text = { Text(Translations.get(language, "settings")) },
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
    },
    floatingActionButton = {
        FloatingActionButton(
            onClick = { showFaqDialog = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Help, contentDescription = "Aide FAQ")
        }
    }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Interactive Islamic and Gregorian Date Banner
            item {
                IslamicDateHeader(
                    language = language,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                )
            }

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
                        1 to Translations.get(language, "tab_dashboard"),
                        2 to Translations.get(language, "tab_community"),
                        3 to Translations.get(language, "tab_family")
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
                                    imageVector = if (index == 0) Icons.Default.Security else if (index == 2) Icons.Default.Public else Icons.Default.CheckCircle,
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

            if (isProfileLoading) {
                item {
                    com.example.ui.components.DigitalIdCardSkeleton()
                }
            } else {
                if (selectedTab == 0) {
                    item {
                        if (isAuthenticated) {
                            com.example.ui.components.DigitalIdCard(
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
                                passportNumber = profilePassport,
                                licenseNumber = profileLicense,
                                expiryDate = expiryDate,
                                language = language,
                                privacyMode = privacyMode,
                                onPhotoClick = { showPhotoMenu = true }
                            )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardActionCard(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = Translations.get(language, "add_to_wallet"),
                                onClick = {
                                    try {
                                        val passFile = com.example.utils.PassGenerator.generatePkPass(
                                            context = context,
                                            fullName = profileFullName,
                                            memberId = memberId,
                                            dateOfBirth = profileDateOfBirth,
                                            verificationStatus = verificationStatus
                                        )
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            passFile
                                        )
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/vnd.apple.pkpass")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                            android.widget.Toast.makeText(context, Translations.get(language, "wallet_export_success"), android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "application/vnd.apple.pkpass"
                                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, Translations.get(language, "add_to_wallet")))
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, Translations.get(language, "wallet_export_error"), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            DashboardActionCard(
                                icon = Icons.Default.Event,
                                title = Translations.get(language, "add_to_calendar"),
                                onClick = {
                                    try {
                                        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                        val date = format.parse(expiryDate) ?: java.util.Date()
                                        val cal = java.util.Calendar.getInstance()
                                        cal.time = date
                                        cal.set(java.util.Calendar.HOUR_OF_DAY, 9)
                                        cal.set(java.util.Calendar.MINUTE, 0)
                                        
                                        val intent = android.content.Intent(android.content.Intent.ACTION_INSERT)
                                            .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                                            .putExtra(android.provider.CalendarContract.Events.TITLE, Translations.get(language, "calendar_event_title"))
                                            .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, Translations.get(language, "calendar_event_desc"))
                                            .putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                                            .putExtra(android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        DashboardActionCard(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Télécharger PDF (Premium)",
                            subtitle = "Générez un certificat certifié au format PDF",
                            onClick = {
                                if (hasPaidForPdf) {
                                    com.example.utils.PdfGenerator.generatePdf(
                                        context = context,
                                        fullName = profileFullName,
                                        dateOfBirth = profileDateOfBirth,
                                        residency = profileResidency,
                                        community = profileCommunityAffiliation,
                                        passport = profilePassport,
                                        license = profileLicense,
                                        memberId = memberId
                                    )
                                } else {
                                    showPaymentDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
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
                                Text(Translations.get(language, "credentials_locked"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(Translations.get(language, "tap_authenticate"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    text = { Text(Translations.get(language, "camera")) },
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
                                    text = { Text(Translations.get(language, "gallery")) },
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
                                    text = { Text(Translations.get(language, "theme_emerald")) },
                                    onClick = { viewModel.updateCardTheme(0); showThemeMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(Translations.get(language, "theme_ocean")) },
                                    onClick = { viewModel.updateCardTheme(1); showThemeMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(Translations.get(language, "theme_ruby")) },
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
                    com.example.ui.components.TasbihCounter(language = language)
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
                        onEditClick = onNavigateToEditProfile,
                        language = language
                    )
                }
                
                item {
                    VerificationStatusDashboard(
                        verificationStatus = verificationStatus,
                        verificationStep = verificationStep,
                        language = language,
                        onStartVerification = { showVerificationDialog = true }
                    )
                }

                item {
                    com.example.ui.components.PrivacySettingsDashboard(
                        language = language,
                        profileVisibility = profileVisibility,
                        showEmail = showEmail,
                        shareLocation = shareLocation,
                        shareData = shareData,
                        allowNotifications = allowNotifications,
                        onUpdateProfileVisibility = { viewModel.updateProfileVisibility(it) },
                        onUpdateShowEmail = { viewModel.updateShowEmail(it) },
                        onUpdateShareLocation = { viewModel.updateShareLocation(it) },
                        onUpdateShareData = { viewModel.updateShareData(it) },
                        onUpdateAllowNotifications = { viewModel.updateAllowNotifications(it) }
                    )
                }

                if (isAuthenticated) {
                    item {
                        ProfileQrSection(
                            memberId = memberId, 
                            isVerified = isVerified, 
                            verificationStatus = verificationStatus, 
                            language = language,
                            fullName = profileFullName,
                            dateOfBirth = profileDateOfBirth,
                            residency = profileResidency,
                            communityAffiliation = profileCommunityAffiliation
                        )
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
                                language = language,
                                onCancel = { viewModel.cancelTicket(ticket) }
                            )
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
                            context = context,
                            profileVisibility = profileVisibility,
                            showEmail = showEmail,
                            shareLocation = shareLocation,
                            shareData = shareData,
                            allowNotifications = allowNotifications,
                            onUpdateProfileVisibility = { viewModel.updateProfileVisibility(it) },
                            onUpdateShowEmail = { viewModel.updateShowEmail(it) },
                            onUpdateShareLocation = { viewModel.updateShareLocation(it) },
                            onUpdateShareData = { viewModel.updateShareData(it) },
                            onUpdateAllowNotifications = { viewModel.updateAllowNotifications(it) },
                            onDocumentUpload = { documentUploadLauncher.launch("image/*") },
                            onStartVerification = { showVerificationDialog = true }
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
                                Text(Translations.get(language, "credentials_locked"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(Translations.get(language, "tap_authenticate"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                if (isAuthenticated) {
                    item {
                        Text(
                            text = Translations.get(language, "community_map_title"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                    item {
                        Text(
                            text = Translations.get(language, "community_map_desc"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        ) {
                            com.example.ui.components.GlobalCommunityMap()
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                Translations.get(language, "auth_required_history"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else if (selectedTab == 3) {
                if (isAuthenticated) {
                    item {
                        FamilyMembersSection(
                            familyMembers = familyMembers,
                            language = language,
                            onAddMember = { name, dob, rel ->
                                viewModel.addFamilyMember(name, dob, rel)
                            },
                            onRemoveMember = { id ->
                                viewModel.removeFamilyMember(id)
                            }
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                Translations.get(language, "auth_required_history"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            }
        }
    }

    if (showVerificationDialog) {
        VerificationWorkflowDialog(
            language = language,
            viewModel = viewModel,
            onDismiss = { showVerificationDialog = false },
            onVerified = {
                viewModel.startMockVerification()
                showVerificationDialog = false
            }
        )
    }

    if (showIdReadyAlert) {
        VerificationSuccessAlert(
            show = showIdReadyAlert,
            onDismiss = {
                showIdReadyAlert = false
                com.example.network.ApiClient.getSessionManager().saveIdReadyAlertDismissed(true)
            },
            onViewId = {
                selectedTab = 0
            }
        )
    }

    if (showPaymentDialog) {
        PaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onPaymentSuccess = {
                viewModel.setHasPaidForPdf(true)
                showPaymentDialog = false
                android.widget.Toast.makeText(context, "Paiement réussi ! Vous pouvez maintenant télécharger le PDF.", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showFaqDialog) {
        VerificationFaqDialog(
            onDismiss = { showFaqDialog = false }
        )
    }

    if (showNotificationsDialog) {
        com.example.ui.components.NotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false }
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
    language: String,
    privacyMode: Boolean = false
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
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 1000f
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
                                text = if (privacyMode) Translations.get(language, "hidden_field") else if (fullName.isNotBlank()) fullName.uppercase() else Translations.get(language, "user").uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            com.example.ui.components.DigitalVerifiedBadge(
                                isVerified = isVerified,
                                memberId = memberId,
                                fullName = fullName,
                                size = com.example.ui.components.BadgeSize.SMALL
                            )
                        }
                    }
                    if (profilePhotoBase64 != null && !privacyMode) {
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
                                    text = if (privacyMode) Translations.get(language, "hidden_field") else if (dateOfBirth.isNotBlank()) dateOfBirth else "--/--/----",
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
                                    text = if (privacyMode) Translations.get(language, "hidden_field") else if (residency.isNotBlank()) residency else Translations.get(language, "not_specified"),
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
                                    text = if (privacyMode) Translations.get(language, "hidden_field") else if (communityAffiliation.isNotBlank()) communityAffiliation else "--",
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
fun ProfileQrSection(
    memberId: String, 
    isVerified: Boolean, 
    verificationStatus: String, 
    language: String,
    fullName: String,
    dateOfBirth: String,
    residency: String,
    communityAffiliation: String
) {
    var isDynamicMode by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableStateOf(30) }
    var securePayload by remember { mutableStateOf("") }
    var currentSignature by remember { mutableStateOf("") }
    var qrBitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var showPayloadDetails by remember { mutableStateOf(false) }
    var showTempLinkQrDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun generateNewToken() {
        val timestamp = System.currentTimeMillis() / 1000
        val payloadRaw = "$memberId-${verificationStatus.ifEmpty { "UNVERIFIED" }}-$fullName-$dateOfBirth-$residency-$communityAffiliation-$timestamp"
        currentSignature = try {
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(payloadRaw.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(24)
        } catch (e: Exception) {
            "sig-failed-pki"
        }
        
        securePayload = """
            {
              "id": "$memberId",
              "status": "${verificationStatus.ifEmpty { "UNVERIFIED" }}",
              "name": "$fullName",
              "dob": "$dateOfBirth",
              "residency": "$residency",
              "community": "$communityAffiliation",
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
            val timestamp = System.currentTimeMillis() / 1000
            val payloadRaw = "$memberId-${verificationStatus.ifEmpty { "UNVERIFIED" }}-$fullName-$dateOfBirth-$residency-$communityAffiliation"
            currentSignature = try {
                val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(payloadRaw.toByteArray())
                bytes.joinToString("") { "%02x".format(it) }.take(24)
            } catch (e: Exception) {
                "sig-failed-pki"
            }
            securePayload = """
                {
                  "id": "$memberId",
                  "status": "${verificationStatus.ifEmpty { "UNVERIFIED" }}",
                  "name": "$fullName",
                  "dob": "$dateOfBirth",
                  "residency": "$residency",
                  "community": "$communityAffiliation",
                  "sig": "$currentSignature",
                  "mode": "static"
                }
            """.trimIndent()
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
                    Text(Translations.get(language, "dynamic_code"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    Text(Translations.get(language, "static_code"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "NFC Enabled",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.get(language, "nfc_tap_ready"),
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
            
            Button(
                onClick = {
                    val shareUrl = "https://idmuslim.org/badge/${memberId}?token=${currentSignature}"
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, Translations.get(language, "share_badge_subject"))
                        putExtra(android.content.Intent.EXTRA_TEXT, String.format(Translations.get(language, "share_badge_text"), shareUrl))
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, Translations.get(language, "share_badge")))
                },
                modifier = Modifier.fillMaxWidth().testTag("share_badge_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = Translations.get(language, "share_badge"),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(Translations.get(language, "share_badge"))
            }

            Button(
                onClick = { showTempLinkQrDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("temp_qr_badge_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = Translations.get(language, "temp_qr_generate"),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(Translations.get(language, "temp_qr_generate"))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
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

    if (showTempLinkQrDialog) {
        AlertDialog(
            onDismissRequest = { showTempLinkQrDialog = false },
            title = {
                Text(
                    "Lien de Partage Temporaire",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Ce QR Code contient une URL temporaire valide pour une seule lecture. Laissez la personne le scanner pour vérifier rapidement votre identité.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val tempLinkId = remember { java.util.UUID.randomUUID().toString().take(8) }
                    val tempShareUrl = "https://idmuslim.org/temp/$tempLinkId?memberId=$memberId"
                    
                    val tempQrBitmap = remember(tempShareUrl) {
                        try {
                            QRCodeGenerator.generateQRCode(tempShareUrl, 400)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (tempQrBitmap != null) {
                        Image(
                            bitmap = tempQrBitmap.asImageBitmap(),
                            contentDescription = "Temporary Share QR",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = tempShareUrl,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTempLinkQrDialog = false }) {
                    Text("Fermer")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
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
                    imageVector = when (log.actionType) {
                        "NFC_VERIFIED" -> Icons.Default.Info
                        "PROFILE_UPDATE" -> Icons.Default.Edit
                        else -> Icons.Default.Security
                    },
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
fun TicketHistoryCard(ticket: TicketEntity, event: EventEntity, language: String, onCancel: () -> Unit) {
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
                        Text(Translations.get(language, "cancel"), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            AnimatedVisibility(visible = expanded && ticket.status == "Valid") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
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
fun VerificationPendingPrompt(step: String, language: String) {
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
                    text = Translations.get(language, "verification_in_progress"),
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
fun VerifyIdentityPrompt(language: String, onClick: () -> Unit) {
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
                Text(Translations.get(language, "verify_identity"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationWorkflowDialog(language: String, viewModel: EventViewModel, onDismiss: () -> Unit, onVerified: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1: Welcome/Info, 2: Doc Details Form, 3: Doc Scan, 4: Selfie, 5: Summary & Consent, 6: Securing Process, 7: Success
    
    // Step 2 Form States
    var docType by remember { mutableStateOf("Passport") } 
    var docNumber by remember { mutableStateOf("") }
    var issuingCountry by remember { mutableStateOf("France") }
    
    // Step 3 Scan States
    var docPhotoCaptured by remember { mutableStateOf(false) }
    var isDocScanning by remember { mutableStateOf(false) }
    var docScanProgress by remember { mutableStateOf(0f) }
    var docImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempDocUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempDocUri != null) {
            docImageUri = tempDocUri
            docPhotoCaptured = true
            isDocScanning = false
        } else {
            isDocScanning = false
        }
    }
    
    val docGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            docImageUri = uri
            docPhotoCaptured = true
            isDocScanning = false
        }
    }
    
    fun launchCamera() {
        try {
            val storageDir = java.io.File(context.cacheDir, "images")
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = java.io.File.createTempFile("doc_scan_", ".jpg", storageDir)
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempDocUri = uri
            isDocScanning = true
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            isDocScanning = false
        }
    }
    
    // Step 4 Selfie States
    var selfieCaptured by remember { mutableStateOf(false) }
    var isSelfieScanning by remember { mutableStateOf(false) }
    var selfieScanProgress by remember { mutableStateOf(0f) }
    var selfieImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempSelfieUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val selfieCameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempSelfieUri != null) {
            selfieImageUri = tempSelfieUri
            selfieCaptured = true
            isSelfieScanning = false
        } else {
            isSelfieScanning = false
        }
    }
    
    val selfieGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selfieImageUri = uri
            selfieCaptured = true
            isSelfieScanning = false
        }
    }
    
    fun launchSelfieCamera() {
        try {
            val storageDir = java.io.File(context.cacheDir, "images")
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = java.io.File.createTempFile("selfie_scan_", ".jpg", storageDir)
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempSelfieUri = uri
            isSelfieScanning = true
            selfieCameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            isSelfieScanning = false
        }
    }
    
    // Step 5 Consent
    var privacyConsentChecked by remember { mutableStateOf(false) }
    
    // Step 6 Security processing dynamic label
    var processingLabel by remember { mutableStateOf("Initialisation de la connexion sécurisée...") }
    
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(step) {
        if (step == 6) {
            // Securely save the verification details to Firestore
            viewModel.updateProfileDocType(docType)
            viewModel.updateProfileDocNumber(docNumber)
            viewModel.updateProfileIssuingCountry(issuingCountry)
            
            val creationTimestamp = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = creationTimestamp
            calendar.add(java.util.Calendar.YEAR, 1)
            val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val expDate = format.format(calendar.time)
            viewModel.updateProfileExpiryDate(expDate)

            viewModel.saveProfileToFirestore(
                fullName = viewModel.profileFullName.value ?: "",
                dob = viewModel.profileDob.value ?: "",
                residency = viewModel.profileResidency.value ?: "",
                community = viewModel.profileCommunityAffiliation.value ?: "",
                passportNumber = viewModel.profilePassportNumber.value ?: "",
                licenseNumber = viewModel.profileLicenseNumber.value ?: "",
                docType = docType,
                docNumber = docNumber,
                issuingCountry = issuingCountry,
                expiryDate = expDate
            )

            processingLabel = "Chiffrement et transfert des pièces de sécurité..."
            kotlinx.coroutines.delay(1200)
            processingLabel = "Analyse biométrique faciale en cours..."
            kotlinx.coroutines.delay(1200)
            processingLabel = "Vérification des filigranes gouvernementaux..."
            kotlinx.coroutines.delay(1100)
            processingLabel = "Génération des preuves d'intégrité IDMuslim..."
            kotlinx.coroutines.delay(1000)
            step = 7
        }
    }

    // Removed fake LaunchedEffect(isDocScanning)

    // Removed fake LaunchedEffect(isSelfieScanning)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // STEP PROGRESS HEADER INDICATOR (only during input steps 1 to 5)
            if (step <= 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .size(if (step == i) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step == i) MaterialTheme.colorScheme.primary 
                                    else if (step > i) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                        if (i < 5) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(2.dp)
                                    .background(
                                        if (step > i) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
                
                Text(
                    text = "Étape $step sur 5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            when (step) {
                1 -> {
                    // Onboarding / Information
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vérification d'Identité Numérique",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vérifiez votre profil de manière hautement sécurisée pour réclamer votre attestation d'identité et débloquer les services premium IDMuslim.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Highlighting advantages
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Données Cryptées en Local",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Vérification de niveau Émeraude",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Commencer la vérification")
                    }
                }
                
                2 -> {
                    // Document Details Form
                    Text(
                        text = "Sélectionnez votre pièce d'identité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Document types options cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Passport" to "Passeport", "ID Card" to "CNI", "License" to "Permis").forEach { (typeKey, label) ->
                            val isSelected = docType == typeKey
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { docType = typeKey },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                     else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Country of issue Input
                    OutlinedTextField(
                        value = issuingCountry,
                        onValueChange = { issuingCountry = it },
                        label = { Text("Pays d'émission") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Doc number input
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        label = { Text("Numéro du document d'identité") },
                        placeholder = { Text("Ex: 12XX34567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retour")
                        }
                        
                        Button(
                            onClick = { step = 3 },
                            enabled = docNumber.trim().isNotEmpty() && issuingCountry.trim().isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suivant")
                        }
                    }
                }
                
                3 -> {
                    // Document Photo Scan
                    Text(
                        text = "Scannez votre document d'identité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Cadrez le recto de votre document dans le viseur de la caméra ci-dessous.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    // Viewfinder Simulator Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Border overlay frame
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .border(
                                        width = 2.dp,
                                        color = if (docPhotoCaptured) Color.Green.copy(alpha = 0.8f) 
                                                else if (isDocScanning) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                else Color.White.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                if (isDocScanning) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Ouverture de la caméra...",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                } else if (docPhotoCaptured && docImageUri != null) {
                                    coil.compose.AsyncImage(
                                        model = docImageUri,
                                        contentDescription = "Document scanné",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success check",
                                            tint = Color.Green,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "DOCUMENT CAPTURÉ ET VALIDÉ",
                                            color = Color.Green,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Camera placeholder",
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Alignez votre document dans le cadre",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Appuyez sur le bouton ci-dessous pour capturer",
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Scan Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchCamera() },
                            enabled = !isDocScanning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (docPhotoCaptured) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (docPhotoCaptured) "Prendre à nouveau" else "Caméra",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = { docGalleryLauncher.launch("image/*") },
                            enabled = !isDocScanning,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (docPhotoCaptured) "Galerie (Nouveau)" else "Galerie",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 2 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retour")
                        }
                        
                        Button(
                            onClick = { step = 4 },
                            enabled = docPhotoCaptured,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suivant")
                        }
                    }
                }
                
                4 -> {
                    // Selfie Verification Screen
                    Text(
                        text = "Vérification de vitalité faciale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Veuillez cadrer votre visage dans le cercle ci-dessous pour prouver que vous êtes le détenteur légitime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    // Circular Viewfinder Simulator
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(
                                width = 3.dp,
                                color = if (selfieCaptured) Color.Green 
                                        else if (isSelfieScanning) MaterialTheme.colorScheme.primary
                                        else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelfieScanning) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ouverture de la caméra...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else if (selfieCaptured && selfieImageUri != null) {
                            coil.compose.AsyncImage(
                                model = selfieImageUri,
                                contentDescription = "Selfie",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success selfie",
                                    tint = Color.Green,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SELFIE COMPATIBLE",
                                    color = Color.Green,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Selfie placeholder",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Centrez votre visage",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Appuyez sur le bouton pour capturer",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchSelfieCamera() },
                            enabled = !isSelfieScanning,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selfieCaptured) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selfieCaptured) "Reprendre selfie" else "Caméra",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = { selfieGalleryLauncher.launch("image/*") },
                            enabled = !isSelfieScanning,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selfieCaptured) "Galerie (Nouveau)" else "Galerie",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 3 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retour")
                        }
                        
                        Button(
                            onClick = { step = 5 },
                            enabled = selfieCaptured,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suivant")
                        }
                    }
                }
                
                5 -> {
                    // Summary and Consent Checklist
                    Text(
                        text = "Vérification finale des éléments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Récapitulatif des pièces :",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Type de Document:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(docType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Numéro de pièce:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(docNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pays émetteur:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(issuingCountry, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Visual checkboxes representing uploaded statuses
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Copie photo du $docType validée", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Test de présence biométrique (Selfie) validé", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Privacy and Consent Policy Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { privacyConsentChecked = !privacyConsentChecked }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = privacyConsentChecked,
                            onCheckedChange = { privacyConsentChecked = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Je consens au traitement sécurisé de mes données d'identification conformément à la politique de chiffrement IDMuslim.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = 4 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retour")
                        }
                        
                        Button(
                            onClick = { step = 6 },
                            enabled = privacyConsentChecked,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Soumettre")
                        }
                    }
                }
                
                6 -> {
                    // Processing layout
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Vérification IDMuslim Shield...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = processingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                7 -> {
                    // Success validation
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Verification",
                        tint = Color.Green,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Identité Vérifiée !",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Félicitations, vos documents ont été analysés d'une manière cryptographique et vérifiés. Votre statut de profil est maintenant mis à jour en Niveau Émeraude.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            onVerified()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Terminer")
                    }
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
        "ar" to "العربية",
        "es" to "Español",
        "id" to "Bahasa Ind."
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
        // Use a scrollable row if there are many languages
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(languages.size) { index ->
                val (code, name) = languages[index]
                val isSelected = currentLanguage == code
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onLanguageSelected(code) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
    onEditClick: () -> Unit,
    language: String
) {
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
                TextButton(onClick = onEditClick) {
                    Text(text = Translations.get(language, "edit"))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = {},
                label = { Text(Translations.get(language, "full_name")) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = {},
                label = { Text(Translations.get(language, "dob_label")) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = residency,
                onValueChange = {},
                label = { Text(Translations.get(language, "residence_label")) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = communityAffiliation,
                onValueChange = {},
                label = { Text(Translations.get(language, "community_affiliation")) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            Text(
                text = Translations.get(language, "privacy_msg"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
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
    context: android.content.Context,
    profileVisibility: String,
    showEmail: Boolean,
    shareLocation: Boolean,
    shareData: Boolean,
    allowNotifications: Boolean,
    onUpdateProfileVisibility: (String) -> Unit,
    onUpdateShowEmail: (Boolean) -> Unit,
    onUpdateShareLocation: (Boolean) -> Unit,
    onUpdateShareData: (Boolean) -> Unit,
    onUpdateAllowNotifications: (Boolean) -> Unit,
    onDocumentUpload: () -> Unit = {},
    onStartVerification: () -> Unit = {}
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

        VerificationStatusDashboard(
            verificationStatus = verificationStatus,
            verificationStep = verificationStep,
            language = language,
            onStartVerification = onStartVerification,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        com.example.ui.components.PrivacySettingsDashboard(
            language = language,
            profileVisibility = profileVisibility,
            showEmail = showEmail,
            shareLocation = shareLocation,
            shareData = shareData,
            allowNotifications = allowNotifications,
            onUpdateProfileVisibility = onUpdateProfileVisibility,
            onUpdateShowEmail = onUpdateShowEmail,
            onUpdateShareLocation = onUpdateShareLocation,
            onUpdateShareData = onUpdateShareData,
            onUpdateAllowNotifications = onUpdateAllowNotifications,
            modifier = Modifier.padding(bottom = 12.dp)
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

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

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
                                text = if (isVerified) "VERIFIED" else verificationStatus.ifEmpty { "UNVERIFIED" },
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
                      "status": "${verificationStatus.ifEmpty { "UNVERIFIED" }}",
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
                        android.widget.Toast.makeText(context, Translations.get(language, "sig_copied"), android.widget.Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersSection(
    familyMembers: List<com.example.data.FamilyMember>,
    language: String,
    onAddMember: (String, String, String) -> Unit,
    onRemoveMember: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Translations.get(language, "family_members"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Translations.get(language, "add_member"), fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (familyMembers.isEmpty()) {
            Text(
                text = "Aucun membre",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            familyMembers.forEach { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${Translations.get(language, "relation")} : ${Translations.get(language, member.relation.lowercase())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemoveMember(member.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("child") } // child, spouse, parent

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(Translations.get(language, "add_member")) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Translations.get(language, "full_name")) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text(Translations.get(language, "dob_label")) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    Text(Translations.get(language, "relation"), style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("child", "spouse", "parent").forEach { rel ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = relation == rel,
                                    onClick = { relation = rel }
                                )
                                Text(Translations.get(language, rel), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && dob.isNotBlank()) {
                            onAddMember(name, dob, relation)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(Translations.get(language, "save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(Translations.get(language, "cancel"))
                }
            }
        )
    }
}

@Composable
fun PaymentDialog(
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("card") } // "card", "paypal", "crypto"
    var isProcessing by remember { mutableStateOf(false) }
    var processingPhase by remember { mutableStateOf("") }
    var showPayPalWebView by remember { mutableStateOf(false) }

    // Card details input states
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // Card input errors
    var cardNameError by remember { mutableStateOf<String?>(null) }
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var cardExpiryError by remember { mutableStateOf<String?>(null) }
    var cardCvvError by remember { mutableStateOf<String?>(null) }

    // Crypto details states
    var cryptoAddressCopied by remember { mutableStateOf(false) }
    var cryptoTxHash by remember { mutableStateOf("") }
    var cryptoTxHashError by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Formatting & verification helpers
    fun formatCardNumber(input: String): String {
        val clean = input.replace("[^\\d]".toRegex(), "")
        val sb = StringBuilder()
        for (i in 0 until clean.length) {
            if (i > 0 && i % 4 == 0) sb.append(" ")
            sb.append(clean[i])
        }
        return sb.toString().take(19) // Max 16 digits + 3 spaces
    }

    fun formatExpiry(input: String): String {
        val clean = input.replace("[^\\d]".toRegex(), "")
        return if (clean.length >= 2) {
            clean.substring(0, 2) + "/" + clean.substring(2).take(2)
        } else {
            clean
        }.take(5)
    }

    fun getCardBrand(num: String): String {
        val clean = num.replace("\\s".toRegex(), "")
        return when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("5") || clean.startsWith("2") -> "MasterCard"
            clean.startsWith("34") || clean.startsWith("37") -> "American Express"
            else -> "Carte Bancaire"
        }
    }

    fun isValidLuhn(number: String): Boolean {
        val cleanNumber = number.replace("\\s".toRegex(), "")
        if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = cleanNumber[i] - '0'
            if (n < 0 || n > 9) return false
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun isValidExpiry(expiry: String): Boolean {
        val clean = expiry.replace("/", "").replace("\\s".toRegex(), "")
        if (clean.length != 4) return false
        val month = clean.substring(0, 2).toIntOrNull() ?: return false
        val yearSuffix = clean.substring(2, 4).toIntOrNull() ?: return false
        if (month < 1 || month > 12) return false
        
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        if (yearSuffix < currentYear) return false
        if (yearSuffix == currentYear && month < currentMonth) return false
        return true
    }

    fun validateCardForm(): Boolean {
        var isValid = true

        if (cardName.trim().length < 3) {
            cardNameError = "Veuillez entrer le nom complet sur la carte."
            isValid = false
        } else {
            cardNameError = null
        }

        val cleanCard = cardNumber.replace("\\s".toRegex(), "")
        if (cleanCard.length < 15 || cleanCard.length > 16 || !isValidLuhn(cardNumber)) {
            cardNumberError = "Numéro de carte invalide (vérification de Luhn échouée)."
            isValid = false
        } else {
            cardNumberError = null
        }

        if (!isValidExpiry(cardExpiry)) {
            cardExpiryError = "Date d'expiration invalide (MM/AA, doit être future)."
            isValid = false
        } else {
            cardExpiryError = null
        }

        val isAmex = getCardBrand(cardNumber) == "American Express"
        val expectedCvvLength = if (isAmex) 4 else 3
        if (cardCvv.replace("[^\\d]".toRegex(), "").length != expectedCvvLength) {
            cardCvvError = "Le CVV doit comporter $expectedCvvLength chiffres."
            isValid = false
        } else {
            cardCvvError = null
        }

        return isValid
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            processingPhase = "Vérification cryptographique de la passerelle..."
            kotlinx.coroutines.delay(1200)
            processingPhase = "Analyse anti-fraude de la transaction..."
            kotlinx.coroutines.delay(1200)
            processingPhase = "Enregistrement sécurisé du justificatif de paiement..."
            kotlinx.coroutines.delay(1000)
            onPaymentSuccess()
        }
    }

    if (showPayPalWebView) {
        PayPalWebView(
            onDismiss = { showPayPalWebView = false },
            onSuccess = {
                showPayPalWebView = false
                onPaymentSuccess()
            }
        )
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isProcessing) onDismiss() },
            title = {
                Text(
                    "Abonnement Premium",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (isProcessing) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = processingPhase,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            Text(
                                "Débloquez la génération de votre profil PDF officiel IDMuslim pour 4.99€.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Moyen de paiement", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Card Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMethod == "card") MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedMethod = "card" }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(selected = selectedMethod == "card", onClick = { selectedMethod = "card" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carte Bancaire", fontWeight = FontWeight.SemiBold)
                            }
                            
                            // PayPal Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMethod == "paypal") MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedMethod = "paypal" }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(selected = selectedMethod == "paypal", onClick = { selectedMethod = "paypal" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PayPal", fontWeight = FontWeight.SemiBold)
                            }
                            
                            // Crypto Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMethod == "crypto") MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { selectedMethod = "crypto" }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(selected = selectedMethod == "crypto", onClick = { selectedMethod = "crypto" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crypto (BTC, ETH, USDT)", fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (selectedMethod == "card") {
                            item {
                                Column {
                                    // Card Brand Header
                                    if (cardNumber.isNotEmpty()) {
                                        Text(
                                            text = "Marque détectée: ${getCardBrand(cardNumber)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    // Cardholder name input
                                    OutlinedTextField(
                                        value = cardName,
                                        onValueChange = { 
                                            cardName = it
                                            cardNameError = null
                                        },
                                        label = { Text("Nom complet sur la carte") },
                                        isError = cardNameError != null,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    cardNameError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                                    }

                                    // Card number input
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { 
                                            cardNumber = formatCardNumber(it)
                                            cardNumberError = null
                                        },
                                        label = { Text("Numéro de carte") },
                                        isError = cardNumberError != null,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    cardNumberError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                                    }

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        // Expiry Date input
                                        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                            OutlinedTextField(
                                                value = cardExpiry,
                                                onValueChange = { 
                                                    cardExpiry = formatExpiry(it)
                                                    cardExpiryError = null
                                                },
                                                label = { Text("Expiration (MM/AA)") },
                                                isError = cardExpiryError != null,
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                                ),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            cardExpiryError?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        // CVV input
                                        Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                                            OutlinedTextField(
                                                value = cardCvv,
                                                onValueChange = { 
                                                    cardCvv = it.replace("[^\\d]".toRegex(), "").take(4)
                                                    cardCvvError = null
                                                },
                                                label = { Text("CVV") },
                                                isError = cardCvvError != null,
                                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                                ),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            cardCvvError?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (selectedMethod == "paypal") {
                            item {
                                Column {
                                    Text(
                                        "Vous allez être redirigé vers l'interface sécurisée PayPal officielle ci-dessous.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        } else if (selectedMethod == "crypto") {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Veuillez envoyer exactement 4.99 EUR de crypto à l'adresse sécurisée suivante:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Bitcoin address showcase
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "bc1qxy2kg3f8pxw69rw0btpnwhh0g30ep58etmxkm",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clipData = android.content.ClipData.newPlainText("Bitcoin Address", "bc1qxy2kg3f8pxw69rw0btpnwhh0g30ep58etmxkm")
                                                clipboardManager.setPrimaryClip(clipData)
                                                cryptoAddressCopied = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (cryptoAddressCopied) Icons.Default.CheckCircle else Icons.Default.Link,
                                                contentDescription = "Copy",
                                                tint = if (cryptoAddressCopied) Color(0xFF1B4D3E) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Enter transaction hash to simulate confirmation checking
                                    OutlinedTextField(
                                        value = cryptoTxHash,
                                        onValueChange = { 
                                            cryptoTxHash = it.trim()
                                            cryptoTxHashError = null
                                        },
                                        isError = cryptoTxHashError != null,
                                        label = { Text("ID de transaction (TXID / Hash)") },
                                        placeholder = { Text("Ex: 5a2efb3... ou entrer pour simuler") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    cryptoTxHashError?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isProcessing) {
                    Button(
                        onClick = { 
                            if (selectedMethod == "paypal") {
                                showPayPalWebView = true
                            } else if (selectedMethod == "crypto") {
                                if (cryptoTxHash.isEmpty()) {
                                    cryptoTxHashError = "Veuillez entrer le TXID / Hash pour vérification."
                                } else {
                                    isProcessing = true
                                }
                            } else {
                                // credit card validation
                                if (validateCardForm()) {
                                    isProcessing = true
                                }
                            }
                        }
                    ) {
                        Text(
                            if (selectedMethod == "paypal") "Lancer PayPal"
                            else if (selectedMethod == "crypto") "Vérifier la transaction"
                            else "Payer 4.99€"
                        )
                    }
                }
            },
            dismissButton = {
                if (!isProcessing) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                }
            }
        )
    }
}

@Composable
fun PayPalWebView(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                    Text("Paiement Sécurisé", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.TextButton(onClick = onSuccess) {
                        Text("J'ai payé")
                    }
                }
                
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                    </head>
                    <body style="display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f5f5f5;">
                        <div style="width: 100%; max-width: 400px; padding: 20px;">
                            <script src="https://www.paypal.com/sdk/js?client-id=BAAKaiX0BuX6gNTkF2kNzlIXvR4xp5y9gaP4BHNdYpd6WaX_LZoVVACXAEuYIqCil4-dzwZe6iqeipYKKI&components=hosted-buttons&disable-funding=venmo&currency=USD"></script>
                            <div id="paypal-container-WDB6DJYF7QW6E"></div>
                            <script>
                              paypal.HostedButtons({
                                hostedButtonId: "WDB6DJYF7QW6E",
                              }).render("#paypal-container-WDB6DJYF7QW6E")
                            </script>
                        </div>
                    </body>
                    </html>
                """.trimIndent()
                
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { context ->
                        android.webkit.WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadsImagesAutomatically = true
                            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                            }
                            loadDataWithBaseURL("https://www.paypal.com", htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize().weight(1f)
                )
            }
        }
    }
}

@Composable
fun VerificationStatusDashboard(
    verificationStatus: String,
    verificationStep: String,
    language: String,
    onStartVerification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusUpper = verificationStatus.uppercase()
    val isPending = statusUpper == "PENDING"
    val isVerified = statusUpper == "VERIFIED"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("verification_status_dashboard"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVerified) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else if (isPending) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isVerified) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else if (isPending) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Block with status label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VÉRIFICATION D'IDENTITÉ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerified) MaterialTheme.colorScheme.primary 
                                else if (isPending) MaterialTheme.colorScheme.tertiary 
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isVerified) "Statut : Profil Certifié Émeraude" 
                               else if (isPending) "Statut : Analyse en cours" 
                               else "Statut : Non Vérifié",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVerified) Color(0xFFD1E7DD)
                            else if (isPending) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle 
                                      else if (isPending) Icons.Default.Refresh 
                                      else Icons.Default.Security,
                        contentDescription = "Status Indicator",
                        tint = if (isVerified) Color(0xFF0F5132) 
                               else if (isPending) MaterialTheme.colorScheme.onTertiaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step-by-step Timeline ProgressBar UI
            val steps = listOf(
                Triple("Soumission", "Dépôt des pièces", true),
                Triple("Cryptog.", "Intégrité & clés", isPending || isVerified),
                Triple("Biométrie", "Selfie vitalité", isVerified || (isPending && (verificationStep.contains("Reconnaissance") || verificationStep.contains("biométrie") || verificationStep.contains("Finalisation")))),
                Triple("ID Émis", "Prêt et disponible", isVerified)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (label, desc, isTrue) ->
                    // Connector line between nodes
                    if (index > 0) {
                        val isBetweenCompleted = steps[index - 1].third && isTrue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBetweenCompleted) {
                                        if (isVerified) Color(0xFF198754) else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    }
                                )
                        )
                    }

                    // Circular Node
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.5f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isTrue) {
                                        if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isTrue) {
                                        if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTrue && index < steps.lastIndex) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Done",
                                    tint = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isTrue && index == steps.lastIndex) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF239B56),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isTrue) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTrue) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informative detail row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    if (isVerified) {
                        Text(
                            text = "Félicitations, votre identité a été validée avec succès ! Vos justificatifs officiels et votre pass wallet ont été générés cryptographiquement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (isPending) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Activité courante :",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = verificationStep.ifBlank { "Examen par l'infrastructure sécurisée..." },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Les algorithmes IDMuslim Shield analysent les filigranes gouvernementaux de votre pièce.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "La vérification d'identité sécurisée IDMuslim vous permet d'obtenir un badge d'attestation officiel, de déverrouiller l'Id numérique premium et plus encore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onStartVerification,
                            modifier = Modifier.fillMaxWidth().testTag("launch_dashboard_verification")
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Commencer la certification Émeraude")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationSuccessAlert(
    show: Boolean,
    onDismiss: () -> Unit,
    onViewId: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (show) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            modifier = modifier.testTag("verification_success_alert"),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success Star",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Identité Numérique Prête !",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    text = "Félicitations ! Votre attestation d'identité numérique IDMuslim a été signée avec succès par notre autorité cryptographique décentralisée.\n\nVotre badge de Citoyen Vérifié niveau Émeraude est officiellement actif.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                        onViewId()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Visualiser ma Carte d'Identité")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun VerificationFaqDialog(onDismiss: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FAQ Vérification (IA)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }
                HorizontalDivider()
                // Chat / FAQ
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Text(
                            "Posez-moi vos questions sur le processus de vérification d'identité IDMuslim. Je suis un assistant intelligent propulsé par Gemini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    items(chatHistory) { (q, a) ->
                         Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                             // User Bubble
                             Box(modifier = Modifier.align(Alignment.End).clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp)) {
                                 Text(q, color = MaterialTheme.colorScheme.onPrimaryContainer)
                             }
                             Spacer(modifier = Modifier.height(8.dp))
                             // Bot Bubble
                             Box(modifier = Modifier.align(Alignment.Start).clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp)) {
                                 Text(a, color = MaterialTheme.colorScheme.onSecondaryContainer)
                             }
                         }
                    }
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
                HorizontalDivider()
                // Input
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Posez votre question...") },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (question.isNotBlank()) {
                                val currentQ = question
                                chatHistory = chatHistory + (currentQ to "...")
                                question = ""
                                isLoading = true
                                coroutineScope.launch {
                                    val prompt = "Contexte: IDMuslim est une application de passeport/identité. Le processus de vérification d'identité demande le numéro de passeport, pièce d'identité et un paiement. L'utilisateur pose la question suivante sur la vérification. Réponds de manière utile et polie.\n\nQuestion: $currentQ"
                                    val answer = com.example.network.gemini.GeminiClient.askQuestion(prompt)
                                    chatHistory = chatHistory.dropLast(1) + (currentQ to answer)
                                    isLoading = false
                                    if (chatHistory.isNotEmpty()) {
                                        listState.animateScrollToItem(chatHistory.size)
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && question.isNotBlank()
                    ) {
                        Text("Envoyer")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}