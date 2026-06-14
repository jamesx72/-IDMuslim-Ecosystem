package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EventEntity
import com.example.data.EventRepository
import com.example.data.TicketEntity
import com.example.network.EmailService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import android.net.Uri
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    private val activityLogDao: com.example.data.ActivityLogDao

    init {
        val database = AppDatabase.getDatabase(application)
        val eventDao = database.eventDao()
        activityLogDao = database.activityLogDao()
        repository = EventRepository(eventDao)
    }

    val activityLogs: StateFlow<List<com.example.data.ActivityLogEntity>> = activityLogDao.getAllLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun logActivity(actionType: String, description: String) {
        viewModelScope.launch {
            activityLogDao.insertLog(
                com.example.data.ActivityLogEntity(
                    timestamp = System.currentTimeMillis(),
                    actionType = actionType,
                    description = description
                )
            )
        }
    }

    private val _isUserVerified = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isUserVerified())
    val isUserVerified: StateFlow<Boolean> = _isUserVerified.asStateFlow()

    private val _verificationStatus = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getVerificationStatus())
    val verificationStatus: StateFlow<String> = _verificationStatus.asStateFlow()

    private val _verificationStep = MutableStateFlow("")
    val verificationStep: StateFlow<String> = _verificationStep.asStateFlow()

    private val _usersList = MutableStateFlow<List<com.example.data.UserDto>>(emptyList())
    val usersList: StateFlow<List<com.example.data.UserDto>> = _usersList.asStateFlow()

    fun loadAllUsers() {
        FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val members = snapshot.documents.map { doc ->
                    val fullName = doc.getString("fullName") ?: "Unknown User"
                    val isVerified = doc.getString("verificationStatus") == "VERIFIED"
                    com.example.data.UserDto(
                        uid = doc.id,
                        fullName = fullName,
                        isVerified = isVerified,
                        dob = doc.getString("dob") ?: "",
                        residency = doc.getString("residency") ?: "",
                        community = doc.getString("community") ?: "",
                        country = doc.getString("country") ?: "",
                        membershipStatus = doc.getString("membershipStatus") ?: "PENDING"
                    )
                }
                _usersList.value = members
            }
    }

    fun toggleUserVerification(uid: String, currentStatus: Boolean) {
        val newStatus = if (currentStatus) "UNVERIFIED" else "VERIFIED"
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(hashMapOf("verificationStatus" to newStatus), com.google.firebase.firestore.SetOptions.merge())
    }

    private val _profilePhotoBase64 = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfilePhotoBase64())
    val profilePhotoBase64: StateFlow<String?> = _profilePhotoBase64.asStateFlow()

    private val _cardTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getCardTheme())
    val cardTheme: StateFlow<Int> = _cardTheme.asStateFlow()

    private val _language = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _prayerNotifications = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPrayerNotifications())
    val prayerNotifications: StateFlow<Boolean> = _prayerNotifications.asStateFlow()

    private val _darkTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getDarkTheme())
    val darkTheme: StateFlow<String> = _darkTheme.asStateFlow()

    private val _privacyMode = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPrivacyMode())
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    private val _profileFullName = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileFullName())
    val profileFullName: StateFlow<String?> = _profileFullName.asStateFlow()

    private val _profileDob = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileDob())
    val profileDob: StateFlow<String?> = _profileDob.asStateFlow()

    private val _profileResidency = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileResidency())
    val profileResidency: StateFlow<String?> = _profileResidency.asStateFlow()

    private val _profileCommunityAffiliation = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileCommunityAffiliation())
    val profileCommunityAffiliation: StateFlow<String?> = _profileCommunityAffiliation.asStateFlow()

    fun setVerificationStatus(status: String) {
        com.example.network.ApiClient.getSessionManager().saveVerificationStatus(status)
        _verificationStatus.value = status
        val isVerified = (status == "VERIFIED")
        com.example.network.ApiClient.getSessionManager().setVerifiedStatus(isVerified)
        _isUserVerified.value = isVerified
        
        if (isVerified) {
            viewModelScope.launch {
                try {
                    val userEmail = com.example.network.ApiClient.getSessionManager().getUserEmail()
                    val fullName = com.example.network.ApiClient.getSessionManager().getProfileFullName() ?: ""
                    EmailService.sendIdentityVerifiedEmail(userEmail, fullName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun verifyIdentity(success: Boolean = true) {
        val status = if (success) "VERIFIED" else "UNVERIFIED"
        setVerificationStatus(status)
    }

    fun invalidateVerification() {
        setVerificationStatus("UNVERIFIED")
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("verificationStatus", "UNVERIFIED")
            } catch (e: Exception) {
                // Document might not exist yet, set it instead
                val data = hashMapOf("verificationStatus" to "UNVERIFIED")
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun syncVerificationStatusFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("verificationStatus") ?: "UNVERIFIED"
                    if (_verificationStatus.value != status) {
                        setVerificationStatus(status)
                    }
                }
            }
    }

    fun uploadDocumentForVerification(uri: Uri, context: Context) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            setVerificationStatus("PENDING")
            _verificationStep.value = "Téléchargement du document en cours..."
            logActivity("INFO", "Dépôt de document d'identité pour vérification")
            
            try {
                val storageRef = FirebaseStorage.getInstance().reference.child("verification_docs/${user.uid}/${System.currentTimeMillis()}.jpg")
                val uploadTask = storageRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val data = hashMapOf(
                            "verificationStatus" to "PENDING",
                            "documentUrl" to downloadUri.toString(),
                            "updatedAt" to System.currentTimeMillis()
                        )
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                        _verificationStep.value = "Document envoyé. En attente de validation..."
                    }
                }.addOnFailureListener {
                    setVerificationStatus("UNVERIFIED")
                    _verificationStep.value = "Échec du téléchargement."
                }
            } catch (e: Exception) {
                setVerificationStatus("UNVERIFIED")
                _verificationStep.value = "Erreur."
            }
        }
    }

    fun startMockVerification() {
        viewModelScope.launch {
            setVerificationStatus("PENDING")
            _verificationStep.value = "Analyse cryptographique du document..."
            logActivity("INFO", "Dépôt des documents d'identité pour vérification")
            kotlinx.coroutines.delay(3000)

            _verificationStep.value = "Reconnaissance faciale et biométrie..."
            kotlinx.coroutines.delay(3000)

            _verificationStep.value = "Finalisation de la validation IDMuslim..."
            kotlinx.coroutines.delay(2000)

            setVerificationStatus("VERIFIED")
            _verificationStep.value = ""
            logActivity("INFO", "Identité vérifiée avec succès par le service d'évaluation biométrique")
        }
    }

    fun updateProfilePhoto(base64: String) {
        com.example.network.ApiClient.getSessionManager().saveProfilePhotoBase64(base64)
        _profilePhotoBase64.value = base64
    }

    fun updateCardTheme(themeIndex: Int) {
        com.example.network.ApiClient.getSessionManager().saveCardTheme(themeIndex)
        _cardTheme.value = themeIndex
    }

    fun updateLanguage(lang: String) {
        com.example.network.ApiClient.getSessionManager().saveLanguage(lang)
        _language.value = lang
    }

    fun updatePrayerNotifications(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().savePrayerNotifications(enabled)
        _prayerNotifications.value = enabled
    }

    fun updateDarkTheme(theme: String) {
        com.example.network.ApiClient.getSessionManager().saveDarkTheme(theme)
        _darkTheme.value = theme
    }

    fun updatePrivacyMode(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().savePrivacyMode(enabled)
        _privacyMode.value = enabled
    }

    fun updateProfileFullName(fullName: String) {
        com.example.network.ApiClient.getSessionManager().saveProfileFullName(fullName)
        _profileFullName.value = fullName
    }

    fun updateProfileDob(dob: String) {
        com.example.network.ApiClient.getSessionManager().saveProfileDob(dob)
        _profileDob.value = dob
    }

    fun updateProfileResidency(residency: String) {
        com.example.network.ApiClient.getSessionManager().saveProfileResidency(residency)
        _profileResidency.value = residency
    }

    fun updateProfileCommunityAffiliation(community: String) {
        com.example.network.ApiClient.getSessionManager().saveProfileCommunityAffiliation(community)
        _profileCommunityAffiliation.value = community
    }

    fun saveProfileToFirestore(fullName: String, dob: String, residency: String, community: String) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "fullName" to fullName,
                    "dob" to dob,
                    "residency" to residency,
                    "community" to community,
                    "updatedAt" to System.currentTimeMillis()
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _familyMembers = MutableStateFlow<List<com.example.data.FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<com.example.data.FamilyMember>> = _familyMembers.asStateFlow()

    fun loadFamilyMembers() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("familyMembers")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val members = snapshot.documents.mapNotNull { it.toObject(com.example.data.FamilyMember::class.java) }
                _familyMembers.value = members
            }
    }

    fun addFamilyMember(fullName: String, dateOfBirth: String, relation: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val ref = FirebaseFirestore.getInstance().collection("users").document(user.uid).collection("familyMembers").document()
        val member = com.example.data.FamilyMember(
            id = ref.id,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            relation = relation
        )
        ref.set(member)
        logActivity("INFO", "Added family member: $fullName")
    }

    fun removeFamilyMember(memberId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("familyMembers").document(memberId).delete()
    }

    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getMemberTickets(memberId: String): StateFlow<List<TicketEntity>> {
        return repository.getTicketsForMember(memberId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun createEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        organizer: String,
        price: Double,
        maxTickets: Int
    ) {
        viewModelScope.launch {
            val event = EventEntity(
                title = title,
                description = description,
                date = date,
                time = time,
                location = location,
                organizer = organizer,
                price = price,
                maxTickets = maxTickets,
                availableTickets = maxTickets
            )
            repository.insertEvent(event)
        }
    }

    fun registerMemberForEvent(eventId: Int, memberId: String, memberEmail: String = "user@example.com", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Early verification check using our Secure Authentication Middleware equivalent locally 
            if (!com.example.network.ApiClient.getSessionManager().isUserVerified()) {
                println("Blocked by Middleware: Unverified member attempted registration.")
                onResult(false)
                return@launch
            }

            try {
                // Simulate backend API call going through the AuthInterceptor
                com.example.network.ApiClient.backendApi.registerForEvent(
                    eventId = eventId,
                    request = com.example.network.RegistrationRequest(
                        eventId = eventId,
                        memberId = memberId,
                        memberEmail = memberEmail
                    )
                )
            } catch (e: Exception) {
                // Since our backend logic is mocked with a fake URL, it will fail here.
                // We'll proceed with local database registration anyway for prototype flow purposes.
                println("Backend API interceptor / call result: ${e.message}")
            }

            val ticket = TicketEntity(
                eventId = eventId,
                memberId = memberId,
                scanCode = UUID.randomUUID().toString(),
                status = "Valid"
            )
            val success = repository.registerForEvent(ticket)
            if (success) {
                // Find event to get the title
                val event = repository.getEventById(eventId)
                event?.let {
                    // Send confirmation email
                    EmailService.sendConfirmationEmail(memberEmail, it.title)
                }
            }
            onResult(success)
        }
    }

    fun joinWaitlist(eventId: Int, memberId: String, memberEmail: String = "user@example.com", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (!com.example.network.ApiClient.getSessionManager().isUserVerified()) {
                onResult(false)
                return@launch
            }
            val success = repository.joinWaitlist(eventId, memberId)
            if (success) {
                val event = repository.getEventById(eventId)
                event?.let {
                    EmailService.sendWaitlistJoinedEmail(memberEmail, it.title)
                }
            }
            onResult(success)
        }
    }

    fun cancelTicket(ticket: TicketEntity) {
        viewModelScope.launch {
            val promotedWaitlistEntry = repository.cancelTicketAndProcessWaitlist(ticket)
            if (promotedWaitlistEntry != null) {
                val event = repository.getEventById(ticket.eventId)
                event?.let {
                    // For prototype, using a fixed mock email for the promoted user
                    EmailService.sendWaitlistPromotedEmail("promoted_user@example.com", it.title)
                }
            }
        }
    }
}
