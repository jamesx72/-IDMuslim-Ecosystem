package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.ui.viewmodels.EventViewModel
import com.example.ui.locales.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current
    
    val profilePhotoBase64 by viewModel.profilePhotoBase64.collectAsState()
    val currentFullName by viewModel.profileFullName.collectAsState()
    val currentDob by viewModel.profileDob.collectAsState()
    val currentResidency by viewModel.profileResidency.collectAsState()
    val currentCommunity by viewModel.profileCommunityAffiliation.collectAsState()
    val currentPassport by viewModel.profilePassportNumber.collectAsState()
    val currentLicense by viewModel.profileLicenseNumber.collectAsState()
    val currentDocType by viewModel.profileDocType.collectAsState()
    val currentDocNumber by viewModel.profileDocNumber.collectAsState()
    val currentIssuingCountry by viewModel.profileIssuingCountry.collectAsState()
    val currentExpiryDate by viewModel.profileExpiryDate.collectAsState()

    var fullName by remember { mutableStateOf(currentFullName ?: "") }
    var dob by remember { mutableStateOf(currentDob ?: "") }
    var residency by remember { mutableStateOf(currentResidency ?: "") }
    var community by remember { mutableStateOf(currentCommunity ?: "") }
    var passportNumber by remember { mutableStateOf(currentPassport ?: "") }
    var licenseNumber by remember { mutableStateOf(currentLicense ?: "") }
    var docType by remember { mutableStateOf(currentDocType ?: "Passport") }
    var docNumber by remember { mutableStateOf(currentDocNumber ?: "") }
    var issuingCountry by remember { mutableStateOf(currentIssuingCountry ?: "") }
    var docExpiryDate by remember { mutableStateOf(currentExpiryDate ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val decodedBitmap by remember(profilePhotoBase64) {
        derivedStateOf {
            if (!profilePhotoBase64.isNullOrEmpty()) {
                try {
                    val decodedString = android.util.Base64.decode(profilePhotoBase64, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    val processImageUri = { uri: android.net.Uri ->
        try {
            val resolver = context.contentResolver
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
            
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(resolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(resolver, uri)
            }
            
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

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            processImageUri(uri)
        }
    }

    LaunchedEffect(currentFullName, currentDob, currentResidency, currentCommunity, currentPassport, currentLicense, currentDocType, currentDocNumber, currentIssuingCountry, currentExpiryDate) {
        if (fullName.isEmpty() && !currentFullName.isNullOrEmpty()) {
            fullName = currentFullName ?: ""
        }
        if (dob.isEmpty() && !currentDob.isNullOrEmpty()) {
            dob = currentDob ?: ""
        }
        if (residency.isEmpty() && !currentResidency.isNullOrEmpty()) {
            residency = currentResidency ?: ""
        }
        if (community.isEmpty() && !currentCommunity.isNullOrEmpty()) {
            community = currentCommunity ?: ""
        }
        if (passportNumber.isEmpty() && !currentPassport.isNullOrEmpty()) {
            passportNumber = currentPassport ?: ""
        }
        if (licenseNumber.isEmpty() && !currentLicense.isNullOrEmpty()) {
            licenseNumber = currentLicense ?: ""
        }
        if (docType.isEmpty() && !currentDocType.isNullOrEmpty()) {
            docType = currentDocType ?: "Passport"
        }
        if (docNumber.isEmpty() && !currentDocNumber.isNullOrEmpty()) {
            docNumber = currentDocNumber ?: ""
        }
        if (issuingCountry.isEmpty() && !currentIssuingCountry.isNullOrEmpty()) {
            issuingCountry = currentIssuingCountry ?: ""
        }
        if (docExpiryDate.isEmpty() && !currentExpiryDate.isNullOrEmpty()) {
            docExpiryDate = currentExpiryDate ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get(language, "edit_profile_title")) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(
                            bitmap = decodedBitmap!!.asImageBitmap(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Camera overlay icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Text(
                Translations.get(language, "personal_info"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(Translations.get(language, "full_name")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("profile_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text(Translations.get(language, "dob_label")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("profile_dob_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = residency,
                onValueChange = { residency = it },
                label = { Text(Translations.get(language, "residence_label")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("profile_country_input"),
                shape = RoundedCornerShape(12.dp)
            )

            var communityExpanded by remember { mutableStateOf(false) }
            var communitySearchResults by remember { mutableStateOf(emptyList<String>()) }
            var isSearchingCommunity by remember { mutableStateOf(false) }

            LaunchedEffect(community) {
                if (community.isBlank() || isSearchingCommunity) {
                    communityExpanded = false
                    return@LaunchedEffect
                }
                kotlinx.coroutines.delay(500)
                try {
                    val apiKey = com.example.BuildConfig.PLACES_API_KEY
                    if (apiKey.isNotEmpty() && apiKey != "YOUR_GOOGLE_PLACES_API_KEY") {
                        val response = com.example.network.PlacesApiClient.api.getPlacePredictions(
                            input = community,
                            apiKey = apiKey
                        )
                        if (response.status == "OK") {
                            communitySearchResults = response.predictions?.map { it.description } ?: emptyList()
                            communityExpanded = communitySearchResults.isNotEmpty()
                        } else {
                            communitySearchResults = emptyList()
                            communityExpanded = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    communitySearchResults = emptyList()
                    communityExpanded = false
                }
            }

            ExposedDropdownMenuBox(
                expanded = communityExpanded,
                onExpandedChange = { communityExpanded = !communityExpanded }
            ) {
                OutlinedTextField(
                    value = community,
                    onValueChange = { 
                        community = it 
                        isSearchingCommunity = false
                    },
                    label = { Text(Translations.get(language, "community_affiliation")) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = communityExpanded,
                    onDismissRequest = { communityExpanded = false }
                ) {
                    communitySearchResults.forEach { result ->
                        DropdownMenuItem(
                            text = { Text(result) },
                            onClick = {
                                community = result
                                isSearchingCommunity = true
                                communityExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = { Text("Numéro de passeport") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = { Text("Numéro de permis") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Détails Officiels de Vérification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            var docTypeMenuExpanded by remember { mutableStateOf(false) }
            val docTypes = listOf("Passport", "CNI (National ID)", "Driver's License")
            ExposedDropdownMenuBox(
                expanded = docTypeMenuExpanded,
                onExpandedChange = { docTypeMenuExpanded = !docTypeMenuExpanded }
            ) {
                OutlinedTextField(
                    value = docType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de document d'identité") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeMenuExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = docTypeMenuExpanded,
                    onDismissRequest = { docTypeMenuExpanded = false }
                ) {
                    docTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                docType = type
                                docTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = docNumber,
                onValueChange = { docNumber = it },
                label = { Text("Numéro du document officiel") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = issuingCountry,
                onValueChange = { issuingCountry = it },
                label = { Text("Pays d'émission") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = docExpiryDate,
                onValueChange = { docExpiryDate = it },
                label = { Text("Date d'expiration du document (JJ/MM/AAAA)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (fullName.isBlank() || dob.isBlank() || residency.isBlank() || community.isBlank()) {
                        errorMessage = Translations.get(language, "error_required_fields")
                        return@Button
                    }
                    val dateRegex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
                    if (!dateRegex.matches(dob)) {
                        errorMessage = Translations.get(language, "error_invalid_date")
                        return@Button
                    }
                    if (docExpiryDate.isNotEmpty() && !dateRegex.matches(docExpiryDate)) {
                        errorMessage = "La date d'expiration doit être au format JJ/MM/AAAA"
                        return@Button
                    }
                    errorMessage = null

                    val hasChanged = (fullName != currentFullName) ||
                                     (dob != currentDob) ||
                                     (residency != currentResidency) ||
                                     (community != currentCommunity) ||
                                     (passportNumber != currentPassport) ||
                                     (licenseNumber != currentLicense) ||
                                     (docType != currentDocType) ||
                                     (docNumber != currentDocNumber) ||
                                     (issuingCountry != currentIssuingCountry) ||
                                     (docExpiryDate != currentExpiryDate)
                                     
                    if (hasChanged) {
                        viewModel.updateProfileFullName(fullName)
                        viewModel.updateProfileDob(dob)
                        viewModel.updateProfileResidency(residency)
                        viewModel.updateProfileCommunityAffiliation(community)
                        viewModel.updateProfilePassportNumber(passportNumber)
                        viewModel.updateProfileLicenseNumber(licenseNumber)
                        viewModel.updateProfileDocType(docType)
                        viewModel.updateProfileDocNumber(docNumber)
                        viewModel.updateProfileIssuingCountry(issuingCountry)
                        viewModel.updateProfileExpiryDate(docExpiryDate)
                        
                        viewModel.saveProfileToFirestore(
                            fullName = fullName,
                            dob = dob,
                            residency = residency,
                            community = community,
                            passportNumber = passportNumber,
                            licenseNumber = licenseNumber,
                            docType = docType,
                            docNumber = docNumber,
                            issuingCountry = issuingCountry,
                            expiryDate = docExpiryDate
                        )

                        viewModel.invalidateVerification() // Triggers re-verification
                        viewModel.logActivity("PROFILE_UPDATE", "User updated personal profile information and triggered ID re-verification.")
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().testTag("profile_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(Translations.get(language, "save_changes"), modifier = Modifier.padding(8.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                Translations.get(language, "reverification_warning"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
