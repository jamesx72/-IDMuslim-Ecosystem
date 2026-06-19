package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodels.EventViewModel
import com.example.ui.locales.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text(Translations.get(language, "dob_label")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = residency,
                onValueChange = { residency = it },
                label = { Text(Translations.get(language, "residence_label")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
                modifier = Modifier.fillMaxWidth(),
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
