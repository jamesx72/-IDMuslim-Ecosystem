package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    var fullName by remember { mutableStateOf(currentFullName ?: "") }
    var dob by remember { mutableStateOf(currentDob ?: "") }
    var residency by remember { mutableStateOf(currentResidency ?: "") }
    var community by remember { mutableStateOf(currentCommunity ?: "") }
    var nationality by remember { mutableStateOf("") }
    var idDocumentNumber by remember { mutableStateOf("") }

    LaunchedEffect(currentFullName, currentDob, currentResidency, currentCommunity) {
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

            OutlinedTextField(
                value = community,
                onValueChange = { community = it },
                label = { Text(Translations.get(language, "community_affiliation")) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = idDocumentNumber,
                onValueChange = { idDocumentNumber = it },
                label = { Text("Identity Document Number") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val hasChanged = (fullName != currentFullName) ||
                                     (dob != currentDob) ||
                                     (residency != currentResidency) ||
                                     (community != currentCommunity) ||
                                     nationality.isNotEmpty() ||
                                     idDocumentNumber.isNotEmpty()
                                     
                    if (hasChanged) {
                        viewModel.updateProfileFullName(fullName)
                        viewModel.updateProfileDob(dob)
                        viewModel.updateProfileResidency(residency)
                        viewModel.updateProfileCommunityAffiliation(community)
                        
                        viewModel.saveProfileToFirestore(fullName, dob, residency, community)
                        
                        // For demonstration, let's also save the additional fields to Firestore.
                        val uUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        uUser?.let { u ->
                            val extraData = hashMapOf<String, Any>(
                                "nationality" to nationality,
                                "idDocumentNumber" to idDocumentNumber
                            )
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(u.uid)
                                .set(extraData, com.google.firebase.firestore.SetOptions.merge())
                        }

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
