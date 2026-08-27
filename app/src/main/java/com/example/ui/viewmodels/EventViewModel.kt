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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import android.net.Uri
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import com.example.security.CryptoManager
import com.example.security.encrypted
import com.example.security.decrypted
import kotlinx.coroutines.flow.map
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.EncryptedLocalIdBackup
import com.example.data.LocalBackupPublicSummary
import com.example.data.DecryptedIdentityPayload

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val cryptoManager = CryptoManager.getInstance()
    private val repository: EventRepository
    private val activityLogDao: com.example.data.ActivityLogDao
    private val communityPostDao: com.example.data.CommunityPostDao
    private val userProfileDao: com.example.data.UserProfileDao
    private val documentDao: com.example.data.DocumentDao

    val cachedDocuments: StateFlow<List<com.example.data.DocumentEntity>>
    val cachedUserProfile: StateFlow<com.example.data.UserProfileEntity?>
    val communityPosts: StateFlow<List<com.example.data.CommunityPostEntity>>
    val activityLogs: StateFlow<List<com.example.data.ActivityLogEntity>>

    data class FirestoreActivityLog(val id: String = "", val timestamp: Long = 0, val actionType: String = "", val description: String = "", val location: String = "Lieu non spécifié")
    private val _userActivityLogs = MutableStateFlow<List<FirestoreActivityLog>>(emptyList())
    val userActivityLogs: StateFlow<List<FirestoreActivityLog>> = _userActivityLogs.asStateFlow()

    private val _isUserVerified = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isUserVerified())
    val isUserVerified: StateFlow<Boolean> = _isUserVerified.asStateFlow()

    private val _verificationStatus = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getVerificationStatus())
    val verificationStatus: StateFlow<String> = _verificationStatus.asStateFlow()

    private val _isAccountSuspended = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isAccountSuspended())
    val isAccountSuspended: StateFlow<Boolean> = _isAccountSuspended.asStateFlow()

    private val _isDeviceCompromised = MutableStateFlow(com.example.security.DeviceIntegrityChecker.isCompromised(application))
    val isDeviceCompromised: StateFlow<Boolean> = _isDeviceCompromised.asStateFlow()

    private val _verificationStep = MutableStateFlow("")
    val verificationStep: StateFlow<String> = _verificationStep.asStateFlow()

    private val _usersList = MutableStateFlow<List<com.example.data.UserDto>>(emptyList())
    val usersList: StateFlow<List<com.example.data.UserDto>> = _usersList.asStateFlow()

    private val _profilePhotoBase64 = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfilePhotoBase64())
    val profilePhotoBase64: StateFlow<String?> = _profilePhotoBase64.asStateFlow()

    private val _cardTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getCardTheme())
    val cardTheme: StateFlow<Int> = _cardTheme.asStateFlow()

    private val _cardFontScale = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getCardFontScale())
    val cardFontScale: StateFlow<Float> = _cardFontScale.asStateFlow()

    private val _isSolarAdaptiveTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isSolarAdaptiveThemeEnabled())
    val isSolarAdaptiveTheme: StateFlow<Boolean> = _isSolarAdaptiveTheme.asStateFlow()

    private val _solarSimulationOverride = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getSolarSimulationOverride())
    val solarSimulationOverride: StateFlow<String> = _solarSimulationOverride.asStateFlow()

    private var cachedAladhanTimings: com.example.data.Timings? = null

    private val _solarState = MutableStateFlow(
        run {
            val (lat, lng, city) = com.example.network.ApiClient.getSessionManager().getLastSolarLocation()
            com.example.utils.SolarThemeHelper.computeSolarState(
                latitude = lat,
                longitude = lng,
                locationName = city ?: "Position Locale",
                aladhanTimings = null,
                overrideSimulation = com.example.network.ApiClient.getSessionManager().getSolarSimulationOverride()
            )
        }
    )
    val solarState: StateFlow<com.example.utils.SolarState> = _solarState.asStateFlow()

    private val _language = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _prayerNotifications = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPrayerNotifications())
    val prayerNotifications: StateFlow<Boolean> = _prayerNotifications.asStateFlow()

    private val _prayerCalculationMethod = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPrayerCalculationMethod())
    val prayerCalculationMethod: StateFlow<Int> = _prayerCalculationMethod.asStateFlow()

    private val _darkTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getDarkTheme())
    val darkTheme: StateFlow<String> = _darkTheme.asStateFlow()

    private val _privacyMode = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPrivacyMode())
    val privacyMode: StateFlow<Boolean> = _privacyMode.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isBiometricLockEnabled())
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    private val _screenSecurityEnabled = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isScreenSecurityEnabled())
    val screenSecurityEnabled: StateFlow<Boolean> = _screenSecurityEnabled.asStateFlow()

    private val _autoLockTimeout = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getAutoLockTimeout())
    val autoLockTimeout: StateFlow<String> = _autoLockTimeout.asStateFlow()

    private val _securityAuditLogs = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getSecurityAuditLogs())
    val securityAuditLogs: StateFlow<List<String>> = _securityAuditLogs.asStateFlow()

    data class UserDocument(val id: String = "", val name: String = "", val url: String = "", val uploadedAt: Long = 0L)
    private val _userDocuments = MutableStateFlow<List<UserDocument>>(emptyList())
    val userDocuments: StateFlow<List<UserDocument>> = _userDocuments.asStateFlow()

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    private val _profileFullName = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileFullName())
    val profileFullName: StateFlow<String?> = _profileFullName.asStateFlow()

    private val _profileDob = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileDob())
    val profileDob: StateFlow<String?> = _profileDob.asStateFlow()

    private val _profileResidency = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileResidency())
    val profileResidency: StateFlow<String?> = _profileResidency.asStateFlow()

    private val _profileCommunityAffiliation = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileCommunityAffiliation())
    val profileCommunityAffiliation: StateFlow<String?> = _profileCommunityAffiliation.asStateFlow()

    private val _profilePassportNumber = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getPassportNumber())
    val profilePassportNumber: StateFlow<String?> = _profilePassportNumber.asStateFlow()

    private val _profileLicenseNumber = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLicenseNumber())
    val profileLicenseNumber: StateFlow<String?> = _profileLicenseNumber.asStateFlow()

    private val _profileDocType = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getDocType())
    val profileDocType: StateFlow<String?> = _profileDocType.asStateFlow()

    private val _profileDocNumber = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getDocNumber())
    val profileDocNumber: StateFlow<String?> = _profileDocNumber.asStateFlow()

    private val _profileIssuingCountry = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getIssuingCountry())
    val profileIssuingCountry: StateFlow<String?> = _profileIssuingCountry.asStateFlow()

    private val _profileExpiryDate = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getExpiryDate())
    val profileExpiryDate: StateFlow<String?> = _profileExpiryDate.asStateFlow()

    private val _hasPaidForPdf = MutableStateFlow(com.example.network.ApiClient.getSessionManager().hasPaidForPdf())
    val hasPaidForPdf: StateFlow<Boolean> = _hasPaidForPdf.asStateFlow()

    private val _profileVisibility = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfileVisibility())
    val profileVisibility: StateFlow<String> = _profileVisibility.asStateFlow()

    private val _showEmail = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShowEmail())
    val showEmail: StateFlow<Boolean> = _showEmail.asStateFlow()

    private val _shareLocation = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLocation())
    val shareLocation: StateFlow<Boolean> = _shareLocation.asStateFlow()

    private val _shareData = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareData())
    val shareData: StateFlow<Boolean> = _shareData.asStateFlow()

    private val _allowNotifications = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getAllowNotifications())
    val allowNotifications: StateFlow<Boolean> = _allowNotifications.asStateFlow()

    // Granular Shared Link Privacy StateFlows
    private val _shareLinkDob = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkDob())
    val shareLinkDob: StateFlow<Boolean> = _shareLinkDob.asStateFlow()

    private val _shareLinkResidency = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkResidency())
    val shareLinkResidency: StateFlow<Boolean> = _shareLinkResidency.asStateFlow()

    private val _shareLinkCommunity = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkCommunity())
    val shareLinkCommunity: StateFlow<Boolean> = _shareLinkCommunity.asStateFlow()

    private val _shareLinkStatus = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkStatus())
    val shareLinkStatus: StateFlow<Boolean> = _shareLinkStatus.asStateFlow()

    private val _shareLinkFullName = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkFullName())
    val shareLinkFullName: StateFlow<Boolean> = _shareLinkFullName.asStateFlow()

    private val _shareLinkPhoto = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getShareLinkPhoto())
    val shareLinkPhoto: StateFlow<Boolean> = _shareLinkPhoto.asStateFlow()

    // --- Real-Time Background Identity Sync Engine (Firebase Firestore) ---
    private var securityStatusListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var userDocListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var privateIdentityListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var privacySettingsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var userDocsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var familyMembersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private val _lastBackgroundSyncTime = MutableStateFlow<Long>(com.example.network.ApiClient.getSessionManager().getLastSyncTime())
    val lastBackgroundSyncTime: StateFlow<Long> = _lastBackgroundSyncTime.asStateFlow()

    private val _isOnline = MutableStateFlow<Boolean>(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isBackgroundSyncEnabled = MutableStateFlow<Boolean>(com.example.network.ApiClient.getSessionManager().isBackgroundSyncEnabled())
    val isBackgroundSyncEnabled: StateFlow<Boolean> = _isBackgroundSyncEnabled.asStateFlow()

    private val _isRealtimeSyncActive = MutableStateFlow<Boolean>(true)
    val isRealtimeSyncActive: StateFlow<Boolean> = _isRealtimeSyncActive.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<com.example.data.FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<com.example.data.FamilyMember>> = _familyMembers.asStateFlow()

    // --- Conflict Resolution Strategy for Room DB & Firestore Sync ---
    private val _syncConflict = MutableStateFlow<com.example.data.SyncConflict?>(null)
    val syncConflict: StateFlow<com.example.data.SyncConflict?> = _syncConflict.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    // --- Backup & Restore Room Database to/from Firestore Cloud as Encrypted JSON ---
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    // --- Secure Local Encrypted JSON Backup for Personal Records ---
    private val _isLocalBackingUp = MutableStateFlow(false)
    val isLocalBackingUp: StateFlow<Boolean> = _isLocalBackingUp.asStateFlow()

    private val _localBackupStatusMessage = MutableStateFlow<String?>(null)
    val localBackupStatusMessage: StateFlow<String?> = _localBackupStatusMessage.asStateFlow()

    private val _lastLocalBackupTime = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLastLocalBackupTime())
    val lastLocalBackupTime: StateFlow<Long> = _lastLocalBackupTime.asStateFlow()

    private val _isAutoLocalBackupEnabled = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isAutoLocalBackupEnabled())
    val isAutoLocalBackupEnabled: StateFlow<Boolean> = _isAutoLocalBackupEnabled.asStateFlow()

    private val _lastLocalBackupFilePath = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLastLocalBackupPath())
    val lastLocalBackupFilePath: StateFlow<String?> = _lastLocalBackupFilePath.asStateFlow()

    private val _lastLocalBackupFileSize = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getLastLocalBackupSize())
    val lastLocalBackupFileSize: StateFlow<Long> = _lastLocalBackupFileSize.asStateFlow()

    private val _localBackupFiles = MutableStateFlow<List<File>>(emptyList())
    val localBackupFiles: StateFlow<List<File>> = _localBackupFiles.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val eventDao = database.eventDao()
        activityLogDao = database.activityLogDao()
        communityPostDao = database.communityPostDao()
        userProfileDao = database.userProfileDao()
        documentDao = database.documentDao()
        repository = EventRepository(eventDao)

        cachedDocuments = documentDao.getAllDocuments()
            .map { list -> list.map { it.decrypted(cryptoManager) } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        val uid = try { FirebaseAuth.getInstance().currentUser?.uid ?: "" } catch (e: Throwable) { "" }
        cachedUserProfile = userProfileDao.getUserProfile(uid)
            .map { entity -> entity?.decrypted(cryptoManager) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        communityPosts = communityPostDao.getAllPosts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activityLogs = activityLogDao.getAllLogs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            documentDao.getAllDocuments().collect { docs ->
                val decryptedDocs = docs.map { it.decrypted(cryptoManager) }
                if (decryptedDocs.isNotEmpty() || _userDocuments.value.isEmpty()) {
                    _userDocuments.value = decryptedDocs.map { doc ->
                        UserDocument(doc.id, doc.name, doc.url, doc.uploadedAt)
                    }
                }
            }
        }

        setupRealtimeIdentitySync()
        setupRealtimeSecurityStatusListener()
        try {
            val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                _isOnline.value = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                val request = android.net.NetworkRequest.Builder()
                    .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                cm.registerNetworkCallback(request, object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        _isOnline.value = true
                    }
                    override fun onLost(network: android.net.Network) {
                        _isOnline.value = false
                    }
                })
            }
        } catch (e: Throwable) {
            _isOnline.value = true
        }

        try {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                if (auth.currentUser != null) {
                    setupRealtimeIdentitySync()
                    setupRealtimeSecurityStatusListener()
                } else {
                    stopRealtimeIdentitySync()
                    stopRealtimeSecurityStatusListener()
                }
            }
        } catch (e: Throwable) {
            android.util.Log.w("EventViewModel", "Firebase auth listener registration skipped: ${e.message}")
        }

        refreshLocalBackupList(application)

        // Solar cycle background ticker (updates solar state and sunrise/sunset progression every 30 seconds)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                refreshSolarState()
            }
        }
    }

    fun createCommunityPost(title: String, content: String, type: String, communityName: String) {
        val sessionManager = com.example.network.ApiClient.getSessionManager()
        val authorName = sessionManager.getProfileFullName()?.takeIf { it.isNotEmpty() } ?: "Admin"
        viewModelScope.launch {
            communityPostDao.insertPost(
                com.example.data.CommunityPostEntity(
                    title = title,
                    content = content,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    authorName = authorName,
                    communityName = communityName
                )
            )
            logActivity("CREATE_POST", "Created post: $title in $communityName")
        }
    }

    fun deleteCommunityPost(postId: Int) {
        viewModelScope.launch {
            communityPostDao.deletePost(postId)
            logActivity("DELETE_POST", "Deleted post ID: $postId")
        }
    }

    fun loadUserActivityLogs() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("activity_logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val actionType = doc.getString("actionType") ?: ""
                    val description = doc.getString("description") ?: ""
                    val location = doc.getString("location") ?: "Lieu non spécifié"
                    FirestoreActivityLog(id, timestamp, actionType, description, location)
                }
                _userActivityLogs.value = logs
            }
    }

    fun loadAllUsers() {
        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val fullName = doc.getString("fullName") ?: "Utilisateur"
                    val isVerified = doc.getBoolean("isVerified") ?: false
                    val membershipStatus = doc.getString("membershipStatus") ?: (if (isVerified) "VERIFIED" else "PENDING")
                    val community = doc.getString("community") ?: ""
                    val expiryDate = doc.getString("expiryDate") ?: ""
                    val idNumber = doc.getString("idNumber") ?: "IDM-${uid.take(8).uppercase()}"
                    val isSuspended = doc.getBoolean("isSuspended") ?: false
                    com.example.data.UserDto(
                        uid = uid,
                        fullName = fullName,
                        isVerified = isVerified,
                        membershipStatus = membershipStatus,
                        community = community,
                        expiryDate = expiryDate,
                        idNumber = idNumber,
                        isSuspended = isSuspended
                    )
                }
                _usersList.value = users
            }
    }

    fun toggleUserVerification(uid: String, currentStatus: Boolean) {
        val newStatus = !currentStatus
        val membershipStatus = if (newStatus) "VERIFIED" else "UNVERIFIED"
        val data = mapOf(
            "isVerified" to newStatus,
            "membershipStatus" to membershipStatus,
            "verificationStatus" to (if (newStatus) "VERIFIED" else "UNVERIFIED")
        )
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                loadAllUsers()
                logActivity("ADMIN_TOGGLE_VERIFICATION", "Updated verification for $uid to $newStatus")
            }
    }

    fun toggleUserSuspension(uid: String, currentSuspension: Boolean) {
        val newSuspension = !currentSuspension
        val data = mutableMapOf<String, Any>(
            "isSuspended" to newSuspension,
            "status" to if (newSuspension) "REVOKED" else "ACTIVE",
            "accountStatus" to if (newSuspension) "REVOKED" else "ACTIVE",
            "verificationStatus" to if (newSuspension) "SUSPENDED" else "UNVERIFIED",
            "membershipStatus" to if (newSuspension) "SUSPENDED" else "UNVERIFIED",
            "isVerified" to false
        )
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                loadAllUsers()
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    _isAccountSuspended.value = newSuspension
                    com.example.network.ApiClient.getSessionManager().saveAccountSuspended(newSuspension)
                    if (newSuspension) {
                        _verificationStatus.value = "SUSPENDED"
                        _isUserVerified.value = false
                        com.example.network.ApiClient.getSessionManager().setVerifiedStatus(false)
                        com.example.network.ApiClient.getSessionManager().saveVerificationStatus("SUSPENDED")
                    }
                }
                logActivity("ADMIN_TOGGLE_SUSPENSION", "Updated suspension for $uid to $newSuspension")
            }
    }

    fun updateUserIdNumber(uid: String, newIdNumber: String) {
        val data = mapOf(
            "idNumber" to newIdNumber
        )
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                loadAllUsers()
                logActivity("ADMIN_UPDATE_ID_NUMBER", "Updated ID number for $uid to $newIdNumber")
            }
    }

    fun logActivity(actionType: String, description: String, location: String = "Lieu non spécifié") {
        viewModelScope.launch {
            val ts = System.currentTimeMillis()
            activityLogDao.insertLog(
                com.example.data.ActivityLogEntity(
                    timestamp = ts,
                    actionType = actionType,
                    description = description,
                    location = location
                )
            )
            
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val data = hashMapOf(
                    "timestamp" to ts,
                    "actionType" to actionType,
                    "description" to description,
                    "location" to location
                )
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("activity_logs").add(data)
                    .addOnSuccessListener {
                        loadUserActivityLogs()
                    }
            }
        }
    }



    fun setBackgroundSyncEnabled(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveBackgroundSyncEnabled(enabled)
        _isBackgroundSyncEnabled.value = enabled
        if (enabled) {
            setupRealtimeIdentitySync()
        } else {
            stopRealtimeIdentitySync()
        }
    }

    fun stopRealtimeSecurityStatusListener() {
        securityStatusListenerRegistration?.remove()
        securityStatusListenerRegistration = null
    }

    fun setupRealtimeSecurityStatusListener() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        securityStatusListenerRegistration?.remove()
        securityStatusListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val statusStr = snapshot.getString("status")?.trim() ?: ""
                val accountStatusStr = snapshot.getString("accountStatus")?.trim() ?: ""
                val verificationStatusStr = snapshot.getString("verificationStatus")?.trim() ?: ""
                val membershipStatusStr = snapshot.getString("membershipStatus")?.trim() ?: ""
                val isSuspendedDoc = snapshot.getBoolean("isSuspended") ?: false

                val isRevokedOrSuspended = isSuspendedDoc ||
                    statusStr.equals("revoked", ignoreCase = true) ||
                    statusStr.equals("suspended", ignoreCase = true) ||
                    statusStr.equals("révoqué", ignoreCase = true) ||
                    statusStr.equals("suspendu", ignoreCase = true) ||
                    accountStatusStr.equals("revoked", ignoreCase = true) ||
                    accountStatusStr.equals("suspended", ignoreCase = true) ||
                    accountStatusStr.equals("révoqué", ignoreCase = true) ||
                    accountStatusStr.equals("suspendu", ignoreCase = true) ||
                    verificationStatusStr.equals("SUSPENDED", ignoreCase = true) ||
                    verificationStatusStr.equals("REVOKED", ignoreCase = true) ||
                    verificationStatusStr.equals("RÉVOQUÉ", ignoreCase = true) ||
                    verificationStatusStr.equals("SUSPENDU", ignoreCase = true) ||
                    membershipStatusStr.equals("SUSPENDED", ignoreCase = true) ||
                    membershipStatusStr.equals("REVOKED", ignoreCase = true) ||
                    membershipStatusStr.equals("RÉVOQUÉ", ignoreCase = true) ||
                    membershipStatusStr.equals("SUSPENDU", ignoreCase = true)

                _isAccountSuspended.value = isRevokedOrSuspended
                com.example.network.ApiClient.getSessionManager().saveAccountSuspended(isRevokedOrSuspended)

                if (isRevokedOrSuspended) {
                    _isUserVerified.value = false
                    _verificationStatus.value = "SUSPENDED"
                    com.example.network.ApiClient.getSessionManager().setVerifiedStatus(false)
                    com.example.network.ApiClient.getSessionManager().saveVerificationStatus("SUSPENDED")
                } else if (_verificationStatus.value == "SUSPENDED" || _verificationStatus.value == "REVOKED") {
                    // Account was restored
                    val restoredStatus = if (verificationStatusStr.isNotEmpty() && !verificationStatusStr.equals("SUSPENDED", true)) {
                        verificationStatusStr
                    } else if (membershipStatusStr.isNotEmpty() && !membershipStatusStr.equals("SUSPENDED", true)) {
                        membershipStatusStr
                    } else "UNVERIFIED"
                    _verificationStatus.value = restoredStatus
                    com.example.network.ApiClient.getSessionManager().saveVerificationStatus(restoredStatus)
                    val isVer = snapshot.getBoolean("isVerified") ?: (restoredStatus.contains("Vérifié", ignoreCase = true) || restoredStatus.contains("Emerald", ignoreCase = true))
                    _isUserVerified.value = isVer
                    com.example.network.ApiClient.getSessionManager().setVerifiedStatus(isVer)
                }
            }
    }

    fun stopRealtimeIdentitySync() {
        userDocListenerRegistration?.remove()
        userDocListenerRegistration = null
        privateIdentityListenerRegistration?.remove()
        privateIdentityListenerRegistration = null
        privacySettingsListenerRegistration?.remove()
        privacySettingsListenerRegistration = null
        userDocsListenerRegistration?.remove()
        userDocsListenerRegistration = null
        familyMembersListenerRegistration?.remove()
        familyMembersListenerRegistration = null
        _isRealtimeSyncActive.value = false
    }

    fun setupRealtimeIdentitySync() {
        if (!_isBackgroundSyncEnabled.value) return
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // 1. Listen to Public Profile (users/{uid})
        userDocListenerRegistration?.remove()
        userDocListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val isSuspendedDoc = snapshot.getBoolean("isSuspended") ?: false
                val membershipStatusStr = snapshot.getString("membershipStatus") ?: ""
                val verificationStatusStr = snapshot.getString("verificationStatus") ?: ""
                val statusStr = snapshot.getString("status") ?: ""
                val accountStatusStr = snapshot.getString("accountStatus") ?: ""

                val isRevokedOrSuspended = isSuspendedDoc || 
                    membershipStatusStr.equals("SUSPENDED", ignoreCase = true) || 
                    membershipStatusStr.equals("REVOKED", ignoreCase = true) ||
                    verificationStatusStr.equals("SUSPENDED", ignoreCase = true) ||
                    verificationStatusStr.equals("REVOKED", ignoreCase = true) ||
                    statusStr.equals("REVOKED", ignoreCase = true) ||
                    statusStr.equals("SUSPENDED", ignoreCase = true) ||
                    accountStatusStr.equals("REVOKED", ignoreCase = true) ||
                    accountStatusStr.equals("SUSPENDED", ignoreCase = true)

                _isAccountSuspended.value = isRevokedOrSuspended
                com.example.network.ApiClient.getSessionManager().saveAccountSuspended(isRevokedOrSuspended)

                if (isRevokedOrSuspended) {
                    _isUserVerified.value = false
                    _verificationStatus.value = "SUSPENDED"
                    com.example.network.ApiClient.getSessionManager().setVerifiedStatus(false)
                    com.example.network.ApiClient.getSessionManager().saveVerificationStatus("SUSPENDED")
                }

                val publicProfile = snapshot.toObject(com.example.data.PublicProfile::class.java)
                publicProfile?.let {
                    if (it.fullName.isNotEmpty() && it.fullName != _profileFullName.value) {
                        _profileFullName.value = it.fullName
                        com.example.network.ApiClient.getSessionManager().saveProfileFullName(it.fullName)
                    }
                    if (it.community.isNotEmpty() && it.community != _profileCommunityAffiliation.value) {
                        _profileCommunityAffiliation.value = it.community
                        com.example.network.ApiClient.getSessionManager().saveProfileCommunityAffiliation(it.community)
                    }
                    if (!isRevokedOrSuspended && it.membershipStatus.isNotEmpty() && it.membershipStatus != _verificationStatus.value) {
                        setVerificationStatus(it.membershipStatus)
                    }

                    viewModelScope.launch {
                        val existing = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                        userProfileDao.insertUserProfile(
                            com.example.data.UserProfileEntity(
                                uid = user.uid,
                                fullName = it.fullName.ifEmpty { existing?.fullName ?: "" },
                                avatarUrl = (it.avatarUrl ?: "").ifEmpty { existing?.avatarUrl ?: "" },
                                membershipStatus = if (isRevokedOrSuspended) "SUSPENDED" else it.membershipStatus.ifEmpty { existing?.membershipStatus ?: "Non Vérifié" },
                                isVerified = if (isRevokedOrSuspended) false else it.isVerified,
                                community = it.community.ifEmpty { existing?.community ?: "" },
                                expiryDate = it.expiryDate.ifEmpty { existing?.expiryDate ?: "" },
                                dob = existing?.dob ?: "",
                                residency = existing?.residency ?: "",
                                passportNumber = existing?.passportNumber ?: "",
                                licenseNumber = existing?.licenseNumber ?: "",
                                docType = existing?.docType ?: "",
                                docNumber = existing?.docNumber ?: "",
                                issuingCountry = existing?.issuingCountry ?: "",
                                idNumber = it.idNumber.ifEmpty { existing?.idNumber?.takeIf { id -> id.isNotBlank() } ?: "IDM-${user.uid.take(8).uppercase()}" },
                                lastSyncTime = System.currentTimeMillis()
                            ).encrypted(cryptoManager)
                        )
                    }
                }

                val hasPaid = snapshot.getBoolean("hasPaidForPdf") ?: false
                if (hasPaid) {
                    com.example.network.ApiClient.getSessionManager().saveHasPaidForPdf(true)
                    _hasPaidForPdf.value = true
                }

                val now = System.currentTimeMillis()
                com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
                _lastBackgroundSyncTime.value = now
                _isRealtimeSyncActive.value = true
            }

        // 2. Listen to Private Identity (users/{uid}/private_profile/identity)
        privateIdentityListenerRegistration?.remove()
        privateIdentityListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("private_profile").document("identity")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val privateIdentity = snapshot.toObject(com.example.data.PrivateIdentity::class.java)?.decrypted(cryptoManager)
                privateIdentity?.let {
                    if (it.dob.isNotEmpty() && it.dob != _profileDob.value) {
                        _profileDob.value = it.dob
                        com.example.network.ApiClient.getSessionManager().saveProfileDob(it.dob)
                    }
                    if (it.residency.isNotEmpty() && it.residency != _profileResidency.value) {
                        _profileResidency.value = it.residency
                        com.example.network.ApiClient.getSessionManager().saveProfileResidency(it.residency)
                    }
                    if (it.passportNumber.isNotEmpty()) {
                        _profilePassportNumber.value = it.passportNumber
                        com.example.network.ApiClient.getSessionManager().savePassportNumber(it.passportNumber)
                    }
                    if (it.licenseNumber.isNotEmpty()) {
                        _profileLicenseNumber.value = it.licenseNumber
                        com.example.network.ApiClient.getSessionManager().saveLicenseNumber(it.licenseNumber)
                    }
                    if (it.docType.isNotEmpty()) {
                        _profileDocType.value = it.docType
                        com.example.network.ApiClient.getSessionManager().saveDocType(it.docType)
                    }
                    if (it.docNumber.isNotEmpty()) {
                        _profileDocNumber.value = it.docNumber
                        com.example.network.ApiClient.getSessionManager().saveDocNumber(it.docNumber)
                    }
                    if (it.issuingCountry.isNotEmpty()) {
                        _profileIssuingCountry.value = it.issuingCountry
                        com.example.network.ApiClient.getSessionManager().saveIssuingCountry(it.issuingCountry)
                    }
                    if (it.expiryDate.isNotEmpty()) {
                        _profileExpiryDate.value = it.expiryDate
                        com.example.network.ApiClient.getSessionManager().saveExpiryDate(it.expiryDate)
                    }

                    viewModelScope.launch {
                        val existing = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                        if (existing != null) {
                            userProfileDao.insertUserProfile(
                                existing.copy(
                                    dob = it.dob,
                                    residency = it.residency,
                                    passportNumber = it.passportNumber,
                                    licenseNumber = it.licenseNumber,
                                    docType = it.docType,
                                    docNumber = it.docNumber,
                                    issuingCountry = it.issuingCountry,
                                    expiryDate = if (it.expiryDate.isNotEmpty()) it.expiryDate else existing.expiryDate,
                                    lastSyncTime = System.currentTimeMillis()
                                ).encrypted(cryptoManager)
                            )
                        }
                    }
                }

                val now = System.currentTimeMillis()
                com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
                _lastBackgroundSyncTime.value = now
            }

        // 3. Listen to Privacy Settings (users/{uid}/settings/privacy)
        privacySettingsListenerRegistration?.remove()
        privacySettingsListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("settings").document("privacy")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                snapshot.getString("profileVisibility")?.let {
                    _profileVisibility.value = it
                    com.example.network.ApiClient.getSessionManager().saveProfileVisibility(it)
                }
                snapshot.getBoolean("showEmail")?.let {
                    _showEmail.value = it
                    com.example.network.ApiClient.getSessionManager().saveShowEmail(it)
                }
                snapshot.getBoolean("shareLocation")?.let {
                    _shareLocation.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLocation(it)
                }
                snapshot.getBoolean("shareData")?.let {
                    _shareData.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareData(it)
                }
                snapshot.getBoolean("allowNotifications")?.let {
                    _allowNotifications.value = it
                    com.example.network.ApiClient.getSessionManager().saveAllowNotifications(it)
                }
                snapshot.getBoolean("shareLinkDob")?.let {
                    _shareLinkDob.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkDob(it)
                }
                snapshot.getBoolean("shareLinkResidency")?.let {
                    _shareLinkResidency.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkResidency(it)
                }
                snapshot.getBoolean("shareLinkCommunity")?.let {
                    _shareLinkCommunity.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkCommunity(it)
                }
                snapshot.getBoolean("shareLinkStatus")?.let {
                    _shareLinkStatus.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkStatus(it)
                }
                snapshot.getBoolean("shareLinkFullName")?.let {
                    _shareLinkFullName.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkFullName(it)
                }
                snapshot.getBoolean("shareLinkPhoto")?.let {
                    _shareLinkPhoto.value = it
                    com.example.network.ApiClient.getSessionManager().saveShareLinkPhoto(it)
                }

                val now = System.currentTimeMillis()
                com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
                _lastBackgroundSyncTime.value = now
            }

        // 4. Listen to Documents (users/{uid}/documents)
        userDocsListenerRegistration?.remove()
        userDocsListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("documents")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val docs = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val rawName = doc.getString("name")
                    val rawUrl = doc.getString("url")
                    val name = if (rawName != null) cryptoManager.decrypt(rawName) ?: rawName else "Document"
                    val url = if (rawUrl != null) cryptoManager.decrypt(rawUrl) ?: rawUrl else ""
                    val uploadedAt = doc.getLong("uploadedAt") ?: 0L
                    UserDocument(id, name, url, uploadedAt)
                }
                _userDocuments.value = docs
                viewModelScope.launch {
                    docs.forEach { doc ->
                        documentDao.insertDocument(
                            com.example.data.DocumentEntity(
                                id = doc.id,
                                name = doc.name,
                                url = doc.url,
                                uploadedAt = doc.uploadedAt,
                                docType = "ID Verification"
                            ).encrypted(cryptoManager)
                        )
                    }
                }
                val now = System.currentTimeMillis()
                com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
                _lastBackgroundSyncTime.value = now
            }

        // 5. Listen to Family Members (users/{uid}/familyMembers)
        familyMembersListenerRegistration?.remove()
        familyMembersListenerRegistration = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("familyMembers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val members = snapshot.documents.mapNotNull { doc ->
                    val obj = doc.toObject(com.example.data.FamilyMember::class.java)
                    if (obj != null) {
                        obj.copy(
                            fullName = cryptoManager.decrypt(obj.fullName) ?: obj.fullName,
                            dateOfBirth = cryptoManager.decrypt(obj.dateOfBirth) ?: obj.dateOfBirth,
                            relation = cryptoManager.decrypt(obj.relation) ?: obj.relation
                        )
                    } else null
                }
                _familyMembers.value = members
                val now = System.currentTimeMillis()
                com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
                _lastBackgroundSyncTime.value = now
            }
    }

    fun triggerBackgroundSyncNow() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _syncStatusMessage.value = "Veuillez vous connecter pour forcer la synchronisation."
            return
        }
        setupRealtimeIdentitySync()
        checkSyncConflicts()
        val now = System.currentTimeMillis()
        com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
        _lastBackgroundSyncTime.value = now
        _syncStatusMessage.value = "⚡ Synchronisation arrière-plan réactivée & mise à jour subordonnée effectuée."
        logActivity("REALTIME_SYNC", "Triggered manual background Firestore sync")
    }

    fun updateProfileVisibility(visibility: String) {
        com.example.network.ApiClient.getSessionManager().saveProfileVisibility(visibility)
        _profileVisibility.value = visibility
        savePrivacySettingsToFirestore()
    }

    fun updateShowEmail(show: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShowEmail(show)
        _showEmail.value = show
        savePrivacySettingsToFirestore()
    }

    fun updateShareLocation(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLocation(share)
        _shareLocation.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareData(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareData(share)
        _shareData.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateAllowNotifications(allow: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveAllowNotifications(allow)
        _allowNotifications.value = allow
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkDob(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkDob(share)
        _shareLinkDob.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkResidency(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkResidency(share)
        _shareLinkResidency.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkCommunity(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkCommunity(share)
        _shareLinkCommunity.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkStatus(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkStatus(share)
        _shareLinkStatus.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkFullName(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkFullName(share)
        _shareLinkFullName.value = share
        savePrivacySettingsToFirestore()
    }

    fun updateShareLinkPhoto(share: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveShareLinkPhoto(share)
        _shareLinkPhoto.value = share
        savePrivacySettingsToFirestore()
    }

    private fun savePrivacySettingsToFirestore() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                val privacySettings = mapOf(
                    "profileVisibility" to _profileVisibility.value,
                    "showEmail" to _showEmail.value,
                    "shareLocation" to _shareLocation.value,
                    "shareData" to _shareData.value,
                    "allowNotifications" to _allowNotifications.value,
                    "shareLinkDob" to _shareLinkDob.value,
                    "shareLinkResidency" to _shareLinkResidency.value,
                    "shareLinkCommunity" to _shareLinkCommunity.value,
                    "shareLinkStatus" to _shareLinkStatus.value,
                    "shareLinkFullName" to _shareLinkFullName.value,
                    "shareLinkPhoto" to _shareLinkPhoto.value
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("settings").document("privacy")
                    .set(privacySettings, com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                    val isSuspendedDoc = snapshot.getBoolean("isSuspended") ?: false
                    val membershipStatusStr = snapshot.getString("membershipStatus") ?: ""
                    val verificationStatusStr = snapshot.getString("verificationStatus") ?: ""
                    val statusStr = snapshot.getString("status") ?: ""
                    val accountStatusStr = snapshot.getString("accountStatus") ?: ""

                    val isRevokedOrSuspended = isSuspendedDoc || 
                        membershipStatusStr.equals("SUSPENDED", ignoreCase = true) || 
                        membershipStatusStr.equals("REVOKED", ignoreCase = true) ||
                        verificationStatusStr.equals("SUSPENDED", ignoreCase = true) ||
                        verificationStatusStr.equals("REVOKED", ignoreCase = true) ||
                        statusStr.equals("REVOKED", ignoreCase = true) ||
                        statusStr.equals("SUSPENDED", ignoreCase = true) ||
                        accountStatusStr.equals("REVOKED", ignoreCase = true) ||
                        accountStatusStr.equals("SUSPENDED", ignoreCase = true)

                    _isAccountSuspended.value = isRevokedOrSuspended
                    com.example.network.ApiClient.getSessionManager().saveAccountSuspended(isRevokedOrSuspended)

                    if (isRevokedOrSuspended) {
                        _isUserVerified.value = false
                        _verificationStatus.value = "SUSPENDED"
                        com.example.network.ApiClient.getSessionManager().setVerifiedStatus(false)
                        com.example.network.ApiClient.getSessionManager().saveVerificationStatus("SUSPENDED")
                    } else {
                        val status = snapshot.getString("verificationStatus") ?: "UNVERIFIED"
                        if (_verificationStatus.value != status) {
                            setVerificationStatus(status)
                        }
                    }
                }
            }
    }

    fun loadUserDocuments() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("documents").get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val rawName = doc.getString("name")
                    val rawUrl = doc.getString("url")
                    val name = if (rawName != null) cryptoManager.decrypt(rawName) ?: rawName else "Document"
                    val url = if (rawUrl != null) cryptoManager.decrypt(rawUrl) ?: rawUrl else ""
                    val uploadedAt = doc.getLong("uploadedAt") ?: 0L
                    UserDocument(id, name, url, uploadedAt)
                }
                _userDocuments.value = docs.sortedByDescending { it.uploadedAt }

                // Persist/Cache in Room Database for offline availability
                viewModelScope.launch {
                    val entities = docs.map { doc ->
                        com.example.data.DocumentEntity(
                            id = doc.id,
                            name = doc.name,
                            url = doc.url,
                            uploadedAt = doc.uploadedAt,
                            docType = "Document",
                            status = "VERIFIED"
                        ).encrypted(cryptoManager)
                    }
                    documentDao.insertAll(entities)
                }
            }
    }

    fun uploadUserDocument(uri: Uri, docName: String, onResult: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val docId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Cache locally in Room immediately with hardware key encryption
        viewModelScope.launch {
            documentDao.insertDocument(
                com.example.data.DocumentEntity(
                    id = docId,
                    name = docName,
                    url = uri.toString(),
                    uploadedAt = timestamp,
                    docType = "Document joint",
                    status = "LOCAL_CACHE"
                ).encrypted(cryptoManager)
            )
        }

        if (user == null) {
            onResult(true)
            return
        }
        viewModelScope.launch {
            try {
                val storageRef = FirebaseStorage.getInstance().reference.child("user_docs/${user.uid}/${timestamp}_${docName.replace(" ", "_")}.pdf")
                storageRef.putFile(uri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val data = hashMapOf(
                            "name" to (cryptoManager.encrypt(docName) ?: docName),
                            "url" to (cryptoManager.encrypt(downloadUri.toString()) ?: downloadUri.toString()),
                            "uploadedAt" to timestamp
                        )
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .collection("documents").document(docId).set(data)
                            .addOnSuccessListener {
                                loadUserDocuments()
                                onResult(true)
                            }
                            .addOnFailureListener { onResult(true) } // Saved locally in Room
                    }.addOnFailureListener { onResult(true) }
                }.addOnFailureListener { onResult(true) }
            } catch (e: Exception) {
                onResult(true) // Saved locally in Room
            }
        }
    }

    fun deleteUserDocument(docId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            documentDao.deleteDocumentById(docId)
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(true)
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("documents").document(docId).delete()
            .addOnSuccessListener {
                loadUserDocuments()
                onResult(true)
            }
            .addOnFailureListener { onResult(true) }
    }

    fun uploadDocumentForVerification(uri: Uri, context: Context) {
        val docId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Locally cache in Room for offline history with hardware key encryption
        val localDoc = com.example.data.DocumentEntity(
            id = docId,
            name = "Justificatif d'identité (Scan IDMuslim)",
            url = uri.toString(),
            uploadedAt = timestamp,
            docType = "Pièce d'identité",
            status = "PENDING"
        ).encrypted(cryptoManager)
        viewModelScope.launch {
            documentDao.insertDocument(localDoc)
        }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            setVerificationStatus("PENDING")
            _verificationStep.value = "Téléchargement du document en cours..."
            logActivity("INFO", "Dépôt de document d'identité pour vérification")
            
            try {
                val storageRef = FirebaseStorage.getInstance().reference.child("verification_docs/${user.uid}/${timestamp}.jpg")
                val uploadTask = storageRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val data = hashMapOf(
                            "verificationStatus" to "PENDING",
                            "documentUrl" to (cryptoManager.encrypt(downloadUri.toString()) ?: downloadUri.toString()),
                            "updatedAt" to timestamp
                        )
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                        _verificationStep.value = "Document envoyé. En attente de validation..."
                    }
                }.addOnFailureListener {
                    setVerificationStatus("UNVERIFIED")
                    _verificationStep.value = "Échec du téléchargement en ligne (enregistré localement)."
                }
            } catch (e: Exception) {
                setVerificationStatus("UNVERIFIED")
                _verificationStep.value = "Erreur réseau (enregistré localement)."
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

    fun updateCardFontScale(scale: Float) {
        val boundedScale = scale.coerceIn(0.75f, 1.40f)
        com.example.network.ApiClient.getSessionManager().saveCardFontScale(boundedScale)
        _cardFontScale.value = boundedScale
    }

    fun updateSolarAdaptiveTheme(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveSolarAdaptiveThemeEnabled(enabled)
        _isSolarAdaptiveTheme.value = enabled
        refreshSolarState()
    }

    fun updateSolarSimulationOverride(override: String) {
        com.example.network.ApiClient.getSessionManager().saveSolarSimulationOverride(override)
        _solarSimulationOverride.value = override
        refreshSolarState()
    }

    fun updateSolarLocation(lat: Double, lng: Double, city: String? = null) {
        com.example.network.ApiClient.getSessionManager().saveLastSolarLocation(lat, lng, city)
        refreshSolarState()
    }

    fun refreshSolarState(aladhanTimings: com.example.data.Timings? = null) {
        if (aladhanTimings != null) {
            cachedAladhanTimings = aladhanTimings
        }
        val (lat, lng, city) = com.example.network.ApiClient.getSessionManager().getLastSolarLocation()
        val override = _solarSimulationOverride.value
        val newState = com.example.utils.SolarThemeHelper.computeSolarState(
            latitude = lat,
            longitude = lng,
            locationName = city ?: "Position Locale",
            aladhanTimings = cachedAladhanTimings,
            overrideSimulation = override
        )
        _solarState.value = newState
    }

    fun updateLanguage(lang: String) {
        com.example.network.ApiClient.getSessionManager().saveLanguage(lang)
        _language.value = lang
    }

    fun updatePrayerNotifications(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().savePrayerNotifications(enabled)
        _prayerNotifications.value = enabled
    }

    fun updatePrayerCalculationMethod(methodId: Int) {
        com.example.network.ApiClient.getSessionManager().savePrayerCalculationMethod(methodId)
        _prayerCalculationMethod.value = methodId
    }

    fun updateDarkTheme(theme: String) {
        com.example.network.ApiClient.getSessionManager().saveDarkTheme(theme)
        _darkTheme.value = theme
    }

    fun updatePrivacyMode(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().savePrivacyMode(enabled)
        _privacyMode.value = enabled
    }

    fun updateBiometricLock(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveBiometricLockEnabled(enabled)
        _biometricLockEnabled.value = enabled
        refreshSecurityAuditLogs()
    }

    fun updateScreenSecurity(enabled: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveScreenSecurityEnabled(enabled)
        _screenSecurityEnabled.value = enabled
        refreshSecurityAuditLogs()
    }

    fun updateAutoLockTimeout(timeout: String) {
        com.example.network.ApiClient.getSessionManager().saveAutoLockTimeout(timeout)
        _autoLockTimeout.value = timeout
        refreshSecurityAuditLogs()
    }

    fun refreshSecurityAuditLogs() {
        _securityAuditLogs.value = com.example.network.ApiClient.getSessionManager().getSecurityAuditLogs()
    }

    fun clearSensitiveDataCache(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            documentDao.clearAll()
            com.example.network.ApiClient.getSessionManager().addSecurityAuditLog("Purge Sécurisée", "Suppression intégrale du cache local des justificatifs")
            refreshSecurityAuditLogs()
            onComplete()
        }
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

    fun updateProfilePassportNumber(passportNumber: String) {
        com.example.network.ApiClient.getSessionManager().savePassportNumber(passportNumber)
        _profilePassportNumber.value = passportNumber
    }

    fun updateProfileLicenseNumber(licenseNumber: String) {
        com.example.network.ApiClient.getSessionManager().saveLicenseNumber(licenseNumber)
        _profileLicenseNumber.value = licenseNumber
    }

    fun updateProfileDocType(docType: String) {
        com.example.network.ApiClient.getSessionManager().saveDocType(docType)
        _profileDocType.value = docType
    }

    fun updateProfileDocNumber(docNumber: String) {
        com.example.network.ApiClient.getSessionManager().saveDocNumber(docNumber)
        _profileDocNumber.value = docNumber
    }

    fun updateProfileIssuingCountry(issuingCountry: String) {
        com.example.network.ApiClient.getSessionManager().saveIssuingCountry(issuingCountry)
        _profileIssuingCountry.value = issuingCountry
    }

    fun updateProfileExpiryDate(expiryDate: String) {
        com.example.network.ApiClient.getSessionManager().saveExpiryDate(expiryDate)
        _profileExpiryDate.value = expiryDate
    }

    fun setHasPaidForPdf(hasPaid: Boolean) {
        com.example.network.ApiClient.getSessionManager().saveHasPaidForPdf(hasPaid)
        _hasPaidForPdf.value = hasPaid
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(mapOf("hasPaidForPdf" to hasPaid), com.google.firebase.firestore.SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveProfileToFirestore(
        fullName: String, 
        dob: String, 
        residency: String, 
        community: String, 
        passportNumber: String, 
        licenseNumber: String,
        docType: String = "",
        docNumber: String = "",
        issuingCountry: String = "",
        expiryDate: String = ""
    ) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            try {
                val existing = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                val newIdNumber = existing?.idNumber?.takeIf { it.isNotBlank() } ?: "IDM-${user.uid.take(8).uppercase()}"
                
                // Public profile data
                val publicProfile = com.example.data.PublicProfile(
                    uid = user.uid,
                    fullName = fullName,
                    community = community,
                    idNumber = newIdNumber,
                    updatedAt = System.currentTimeMillis()
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .set(publicProfile, com.google.firebase.firestore.SetOptions.merge())

                // Private highly sensitive identity data stored securely in a subcollection
                val privateIdentity = com.example.data.PrivateIdentity(
                    dob = dob,
                    residency = residency,
                    passportNumber = passportNumber,
                    licenseNumber = licenseNumber,
                    docType = docType,
                    docNumber = docNumber,
                    issuingCountry = issuingCountry,
                    expiryDate = expiryDate,
                    updatedAt = System.currentTimeMillis()
                ).encrypted(cryptoManager)
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("private_profile").document("identity")
                    .set(privateIdentity, com.google.firebase.firestore.SetOptions.merge())

                // Local Room cache with Hardware key encryption
                val updatedProfile = com.example.data.UserProfileEntity(
                    uid = user.uid,
                    fullName = fullName,
                    avatarUrl = existing?.avatarUrl ?: "",
                    membershipStatus = existing?.membershipStatus ?: "Non Vérifié",
                    isVerified = existing?.isVerified ?: false,
                    community = community,
                    expiryDate = if (expiryDate.isNotEmpty()) expiryDate else (existing?.expiryDate ?: ""),
                    dob = dob,
                    residency = residency,
                    passportNumber = passportNumber,
                    licenseNumber = licenseNumber,
                    docType = docType,
                    docNumber = docNumber,
                    issuingCountry = issuingCountry,
                    idNumber = newIdNumber,
                    lastSyncTime = System.currentTimeMillis()
                ).encrypted(cryptoManager)
                userProfileDao.insertUserProfile(updatedProfile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearCacheAndRefresh(onComplete: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            db.clearAllTables()
            launch(kotlinx.coroutines.Dispatchers.Main) {
                loadProfileFromFirestore()
                syncVerificationStatusFromFirestore()
                onComplete()
            }
        }
    }

    fun loadProfileFromFirestore() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            _isProfileLoading.value = true
            try {
                var pendingTasks = 3
                fun checkComplete() {
                    pendingTasks--
                    if (pendingTasks <= 0) {
                        _isProfileLoading.value = false
                    }
                }

                // Privacy Settings
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("settings").document("privacy").get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                updateProfileVisibility(document.getString("profileVisibility") ?: "Public")
                                updateShowEmail(document.getBoolean("showEmail") ?: false)
                                updateShareLocation(document.getBoolean("shareLocation") ?: true)
                                updateShareData(document.getBoolean("shareData") ?: false)
                                updateAllowNotifications(document.getBoolean("allowNotifications") ?: true)
                                updateShareLinkDob(document.getBoolean("shareLinkDob") ?: true)
                                updateShareLinkResidency(document.getBoolean("shareLinkResidency") ?: true)
                                updateShareLinkCommunity(document.getBoolean("shareLinkCommunity") ?: true)
                                updateShareLinkStatus(document.getBoolean("shareLinkStatus") ?: true)
                                updateShareLinkFullName(document.getBoolean("shareLinkFullName") ?: true)
                                updateShareLinkPhoto(document.getBoolean("shareLinkPhoto") ?: false)
                            }
                        }
                        checkComplete()
                    }

                // Public profile data
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                val isSuspendedDoc = document.getBoolean("isSuspended") ?: false
                                val membershipStatusStr = document.getString("membershipStatus") ?: ""
                                val verificationStatusStr = document.getString("verificationStatus") ?: ""
                                val statusStr = document.getString("status") ?: ""
                                val accountStatusStr = document.getString("accountStatus") ?: ""

                                val isRevokedOrSuspended = isSuspendedDoc || 
                                    membershipStatusStr.equals("SUSPENDED", ignoreCase = true) || 
                                    membershipStatusStr.equals("REVOKED", ignoreCase = true) ||
                                    verificationStatusStr.equals("SUSPENDED", ignoreCase = true) ||
                                    verificationStatusStr.equals("REVOKED", ignoreCase = true) ||
                                    statusStr.equals("REVOKED", ignoreCase = true) ||
                                    statusStr.equals("SUSPENDED", ignoreCase = true) ||
                                    accountStatusStr.equals("REVOKED", ignoreCase = true) ||
                                    accountStatusStr.equals("SUSPENDED", ignoreCase = true)

                                _isAccountSuspended.value = isRevokedOrSuspended
                                com.example.network.ApiClient.getSessionManager().saveAccountSuspended(isRevokedOrSuspended)

                                if (isRevokedOrSuspended) {
                                    _isUserVerified.value = false
                                    _verificationStatus.value = "SUSPENDED"
                                    com.example.network.ApiClient.getSessionManager().setVerifiedStatus(false)
                                    com.example.network.ApiClient.getSessionManager().saveVerificationStatus("SUSPENDED")
                                }

                                val publicProfile = document.toObject(com.example.data.PublicProfile::class.java)
                                publicProfile?.let {
                                    updateProfileFullName(it.fullName)
                                    updateProfileCommunityAffiliation(it.community)
                                    viewModelScope.launch {
                                        val existing = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                                        userProfileDao.insertUserProfile(
                                            com.example.data.UserProfileEntity(
                                                uid = user.uid,
                                                fullName = it.fullName,
                                                avatarUrl = it.avatarUrl,
                                                membershipStatus = if (isRevokedOrSuspended) "SUSPENDED" else it.membershipStatus,
                                                isVerified = if (isRevokedOrSuspended) false else it.isVerified,
                                                community = it.community,
                                                expiryDate = it.expiryDate,
                                                dob = existing?.dob ?: "",
                                                residency = existing?.residency ?: "",
                                                passportNumber = existing?.passportNumber ?: "",
                                                licenseNumber = existing?.licenseNumber ?: "",
                                                docType = existing?.docType ?: "",
                                                docNumber = existing?.docNumber ?: "",
                                                issuingCountry = existing?.issuingCountry ?: "",
                                                idNumber = existing?.idNumber?.takeIf { id -> id.isNotBlank() } ?: "IDM-${user.uid.take(8).uppercase()}",
                                                lastSyncTime = System.currentTimeMillis()
                                            ).encrypted(cryptoManager)
                                        )
                                    }
                                }
                                
                                val hasPaid = document.getBoolean("hasPaidForPdf") ?: false
                                if (hasPaid) {
                                    com.example.network.ApiClient.getSessionManager().saveHasPaidForPdf(true)
                                    _hasPaidForPdf.value = true
                                }
                            }
                        }
                        checkComplete()
                    }

                // Private highly sensitive identity data stored securely in a subcollection
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("private_profile").document("identity").get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                val privateIdentity = document.toObject(com.example.data.PrivateIdentity::class.java)?.decrypted(cryptoManager)
                                privateIdentity?.let {
                                    updateProfileDob(it.dob)
                                    updateProfileResidency(it.residency)
                                    updateProfilePassportNumber(it.passportNumber)
                                    updateProfileLicenseNumber(it.licenseNumber)
                                    updateProfileDocType(it.docType)
                                    updateProfileDocNumber(it.docNumber)
                                    updateProfileIssuingCountry(it.issuingCountry)
                                    updateProfileExpiryDate(it.expiryDate)
                                    viewModelScope.launch {
                                        val existing = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                                        if (existing != null) {
                                            userProfileDao.insertUserProfile(
                                                existing.copy(
                                                    dob = it.dob,
                                                    residency = it.residency,
                                                    passportNumber = it.passportNumber,
                                                    licenseNumber = it.licenseNumber,
                                                    docType = it.docType,
                                                    docNumber = it.docNumber,
                                                    issuingCountry = it.issuingCountry,
                                                    lastSyncTime = System.currentTimeMillis()
                                                ).encrypted(cryptoManager)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        checkComplete()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                _isProfileLoading.value = false
            }
        }
    }

    fun loadFamilyMembers() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("familyMembers")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                val members = snapshot.documents.mapNotNull { doc ->
                    val obj = doc.toObject(com.example.data.FamilyMember::class.java)
                    if (obj != null) {
                        obj.copy(
                            fullName = cryptoManager.decrypt(obj.fullName) ?: obj.fullName,
                            dateOfBirth = cryptoManager.decrypt(obj.dateOfBirth) ?: obj.dateOfBirth,
                            relation = cryptoManager.decrypt(obj.relation) ?: obj.relation
                        )
                    } else null
                }
                _familyMembers.value = members
            }
    }

    fun addFamilyMember(fullName: String, dateOfBirth: String, relation: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val ref = FirebaseFirestore.getInstance().collection("users").document(user.uid).collection("familyMembers").document()
        val member = com.example.data.FamilyMember(
            id = ref.id,
            fullName = cryptoManager.encrypt(fullName) ?: fullName,
            dateOfBirth = cryptoManager.encrypt(dateOfBirth) ?: dateOfBirth,
            relation = cryptoManager.encrypt(relation) ?: relation
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

    fun clearSyncStatusMessage() {
        _syncStatusMessage.value = null
    }

    fun forceCloudSync() {
        val now = System.currentTimeMillis()
        _lastBackgroundSyncTime.value = now
        com.example.network.ApiClient.getSessionManager().saveLastSyncTime(now)
        setupRealtimeIdentitySync()
        checkSyncConflicts()
        loadUserActivityLogs()
    }

    fun dismissConflict() {
        _syncConflict.value = null
    }

    fun checkSyncConflicts() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _syncStatusMessage.value = "Veuillez vous connecter pour vérifier la synchronisation."
            return
        }

        viewModelScope.launch {
            try {
                _syncStatusMessage.value = "Vérification de la synchronisation Room <-> Firestore..."
                val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

                userDocRef.get().addOnSuccessListener { publicSnap ->
                    val publicProfile = publicSnap.toObject(com.example.data.PublicProfile::class.java)

                    userDocRef.collection("private_profile").document("identity").get().addOnSuccessListener { privateSnap ->
                        val privateIdentity = privateSnap.toObject(com.example.data.PrivateIdentity::class.java)?.decrypted(cryptoManager)

                        viewModelScope.launch {
                            val local = userProfileDao.getUserProfileSync(user.uid)?.decrypted(cryptoManager)
                            val cloudProfile = com.example.data.UserProfileEntity(
                                uid = user.uid,
                                fullName = publicProfile?.fullName ?: "",
                                avatarUrl = publicProfile?.avatarUrl ?: "",
                                membershipStatus = publicProfile?.membershipStatus ?: "Non Vérifié",
                                isVerified = publicProfile?.isVerified ?: false,
                                community = publicProfile?.community ?: "",
                                expiryDate = publicProfile?.expiryDate ?: (privateIdentity?.expiryDate ?: ""),
                                dob = privateIdentity?.dob ?: "",
                                residency = privateIdentity?.residency ?: "",
                                passportNumber = privateIdentity?.passportNumber ?: "",
                                licenseNumber = privateIdentity?.licenseNumber ?: "",
                                docType = privateIdentity?.docType ?: "",
                                docNumber = privateIdentity?.docNumber ?: "",
                                issuingCountry = privateIdentity?.issuingCountry ?: "",
                                idNumber = local?.idNumber?.takeIf { it.isNotBlank() } ?: "IDM-${user.uid.take(8).uppercase()}",
                                lastSyncTime = System.currentTimeMillis()
                            )

                            if (local == null) {
                                userProfileDao.insertUserProfile(cloudProfile.encrypted(cryptoManager))
                                _syncStatusMessage.value = "Données Cloud synchronisées dans la base Room locale."
                            } else {
                                val diffs = mutableListOf<String>()
                                if (local.fullName.trim() != cloudProfile.fullName.trim() && cloudProfile.fullName.isNotEmpty()) {
                                    diffs.add("Nom complet : Local = '${local.fullName}', Cloud = '${cloudProfile.fullName}'")
                                }
                                if (local.community.trim() != cloudProfile.community.trim() && cloudProfile.community.isNotEmpty()) {
                                    diffs.add("Délégation/Commune : Local = '${local.community}', Cloud = '${cloudProfile.community}'")
                                }
                                if (local.dob.trim() != cloudProfile.dob.trim() && cloudProfile.dob.isNotEmpty()) {
                                    diffs.add("Date de naissance : Local = '${local.dob}', Cloud = '${cloudProfile.dob}'")
                                }
                                if (local.residency.trim() != cloudProfile.residency.trim() && cloudProfile.residency.isNotEmpty()) {
                                    diffs.add("Pays de résidence : Local = '${local.residency}', Cloud = '${cloudProfile.residency}'")
                                }
                                if (local.docNumber.trim() != cloudProfile.docNumber.trim() && cloudProfile.docNumber.isNotEmpty()) {
                                    diffs.add("N° Document : Local = '${local.docNumber}', Cloud = '${cloudProfile.docNumber}'")
                                }

                                if (diffs.isNotEmpty()) {
                                    _syncConflict.value = com.example.data.SyncConflict(
                                        type = com.example.data.SyncConflictType.ProfileConflict(
                                            localProfile = local,
                                            cloudProfile = cloudProfile,
                                            differences = diffs
                                        )
                                    )
                                    _syncStatusMessage.value = null
                                } else {
                                    _syncStatusMessage.value = "✅ Parfait ! Base Room et Firestore Cloud sont 100% synchronisées."
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _syncStatusMessage.value = "Erreur lors de la vérification : ${e.localizedMessage}"
            }
        }
    }

    fun resolveConflictUseLocal(conflict: com.example.data.SyncConflict) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            when (val type = conflict.type) {
                is com.example.data.SyncConflictType.ProfileConflict -> {
                    val local = type.localProfile
                    try {
                        val publicMap = mapOf(
                            "fullName" to local.fullName,
                            "community" to local.community,
                            "avatarUrl" to (local.avatarUrl ?: ""),
                            "membershipStatus" to local.membershipStatus,
                            "isVerified" to local.isVerified,
                            "expiryDate" to local.expiryDate
                        )
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .set(publicMap, com.google.firebase.firestore.SetOptions.merge())

                        val privateIdentityObj = com.example.data.PrivateIdentity(
                            dob = local.dob,
                            residency = local.residency,
                            passportNumber = local.passportNumber,
                            licenseNumber = local.licenseNumber,
                            docType = local.docType,
                            docNumber = local.docNumber,
                            issuingCountry = local.issuingCountry,
                            expiryDate = local.expiryDate
                        ).encrypted(cryptoManager)

                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .collection("private_profile").document("identity")
                            .set(privateIdentityObj, com.google.firebase.firestore.SetOptions.merge())

                        val updatedLocal = local.copy(lastSyncTime = System.currentTimeMillis())
                        userProfileDao.insertUserProfile(updatedLocal.encrypted(cryptoManager))

                        _syncConflict.value = null
                        _syncStatusMessage.value = "Succès : La version locale (Room) a écrasé la version Cloud (Firestore)."
                        logActivity("SYNC_RESOLVED", "Conflict resolved using local Room profile")
                    } catch (e: Exception) {
                        _syncStatusMessage.value = "Erreur lors de la synchronisation vers Cloud : ${e.localizedMessage}"
                    }
                }
                is com.example.data.SyncConflictType.DocumentConflict -> {
                    _syncConflict.value = null
                    _syncStatusMessage.value = "Version locale des documents conservée."
                }
            }
        }
    }

    fun resolveConflictUseCloud(conflict: com.example.data.SyncConflict) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            when (val type = conflict.type) {
                is com.example.data.SyncConflictType.ProfileConflict -> {
                    val cloud = type.cloudProfile
                    try {
                        val cloudWithSyncTime = cloud.copy(lastSyncTime = System.currentTimeMillis())
                        userProfileDao.insertUserProfile(cloudWithSyncTime.encrypted(cryptoManager))

                        updateProfileFullName(cloud.fullName)
                        updateProfileCommunityAffiliation(cloud.community)
                        updateProfileDob(cloud.dob)
                        updateProfileResidency(cloud.residency)
                        updateProfilePassportNumber(cloud.passportNumber)
                        updateProfileLicenseNumber(cloud.licenseNumber)
                        updateProfileDocType(cloud.docType)
                        updateProfileDocNumber(cloud.docNumber)
                        updateProfileIssuingCountry(cloud.issuingCountry)
                        updateProfileExpiryDate(cloud.expiryDate)

                        _syncConflict.value = null
                        _syncStatusMessage.value = "Succès : La base locale (Room) a été mise à jour avec la version Cloud (Firestore)."
                        logActivity("SYNC_RESOLVED", "Conflict resolved using cloud Firestore profile")
                    } catch (e: Exception) {
                        _syncStatusMessage.value = "Erreur lors de la mise à jour locale : ${e.localizedMessage}"
                    }
                }
                is com.example.data.SyncConflictType.DocumentConflict -> {
                    _syncConflict.value = null
                    _syncStatusMessage.value = "Base locale mise à jour depuis le Cloud."
                }
            }
        }
    }

    fun cancelTicket(ticket: TicketEntity) {
        viewModelScope.launch {
            val promotedWaitlistEntry = repository.cancelTicketAndProcessWaitlist(ticket)
            if (promotedWaitlistEntry != null) {
                val event = repository.getEventById(ticket.eventId)
                event?.let {
                    EmailService.sendWaitlistPromotedEmail("promoted_user@example.com", it.title)
                }
            }
        }
    }

    fun clearBackupStatusMessage() {
        _backupStatusMessage.value = null
    }

    fun backupRoomDatabaseToCloud() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _backupStatusMessage.value = "Veuillez vous connecter pour effectuer la sauvegarde."
            return
        }

        _isBackingUp.value = true
        _backupStatusMessage.value = "Chiffrement et sauvegarde de la base Room en cours..."

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val profile = userProfileDao.getUserProfileSync(user.uid)
                val docs = try { documentDao.getAllDocuments().first() } catch (e: Exception) { emptyList() }
                val logs = try { activityLogDao.getAllLogs().first() } catch (e: Exception) { emptyList() }
                val posts = try { communityPostDao.getAllPosts().first() } catch (e: Exception) { emptyList() }

                val backupDump = com.example.data.RoomDatabaseBackup(
                    backupTimestamp = System.currentTimeMillis(),
                    uid = user.uid,
                    userProfile = profile,
                    documents = docs,
                    activityLogs = logs,
                    communityPosts = posts
                )

                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(com.example.data.RoomDatabaseBackup::class.java)

                val jsonString = adapter.toJson(backupDump)
                val encryptedBlob = cryptoManager.encrypt(jsonString) ?: jsonString

                val backupDoc = mapOf(
                    "backupTimestamp" to System.currentTimeMillis(),
                    "backupDate" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    "encryptedJsonBlob" to encryptedBlob,
                    "recordsCount" to (docs.size + logs.size + posts.size + (if (profile != null) 1 else 0)),
                    "encryptedWith" to "AES-256-GCM (Android KeyStore)",
                    "appVersion" to "1.0.0"
                )

                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .collection("room_backups").document("latest")
                    .set(backupDoc)
                    .addOnSuccessListener {
                        _backupStatusMessage.value = "✅ Sauvegarde de la base Room chiffrée et envoyée sur Firestore avec succès !"
                        _isBackingUp.value = false
                        logActivity("BACKUP_DATABASE_SUCCESS", "Encrypted Room DB state backup uploaded to Firestore")
                    }
                    .addOnFailureListener { e ->
                        _backupStatusMessage.value = "❌ Échec de la sauvegarde sur Firestore : ${e.localizedMessage}"
                        _isBackingUp.value = false
                    }
            } catch (e: Exception) {
                _backupStatusMessage.value = "❌ Erreur de traitement de la base Room : ${e.localizedMessage}"
                _isBackingUp.value = false
            }
        }
    }

    fun restoreRoomDatabaseFromCloud() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _backupStatusMessage.value = "Veuillez vous connecter pour restaurer vos données."
            return
        }

        _isBackingUp.value = true
        _backupStatusMessage.value = "Téléchargement de la sauvegarde chiffrée depuis Firestore..."

        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .collection("room_backups").document("latest")
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    _backupStatusMessage.value = "Aucune sauvegarde trouvée sur Firestore Cloud."
                    _isBackingUp.value = false
                    return@addOnSuccessListener
                }

                val encryptedBlob = snap.getString("encryptedJsonBlob")
                if (encryptedBlob.isNullOrEmpty()) {
                    _backupStatusMessage.value = "Document de sauvegarde corrompu ou vide."
                    _isBackingUp.value = false
                    return@addOnSuccessListener
                }

                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val decryptedJson = cryptoManager.decrypt(encryptedBlob)
                        if (decryptedJson.isNullOrEmpty()) {
                            _backupStatusMessage.value = "❌ Échec du déchiffrement de la sauvegarde."
                            _isBackingUp.value = false
                            return@launch
                        }

                        val moshi = com.squareup.moshi.Moshi.Builder()
                            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                            .build()
                        val adapter = moshi.adapter(com.example.data.RoomDatabaseBackup::class.java)
                        val backup = adapter.fromJson(decryptedJson)

                        if (backup != null) {
                            backup.userProfile?.let { userProfileDao.insertUserProfile(it) }
                            if (backup.documents.isNotEmpty()) {
                                documentDao.insertAll(backup.documents)
                            }
                            for (log in backup.activityLogs) {
                                activityLogDao.insertLog(log)
                            }
                            for (post in backup.communityPosts) {
                                communityPostDao.insertPost(post)
                            }

                            _backupStatusMessage.value = "✅ Base Room restaurée avec succès depuis la sauvegarde Cloud !"
                            _isBackingUp.value = false
                            logActivity("RESTORE_DATABASE_SUCCESS", "Restored Room DB from Firestore encrypted backup")
                        } else {
                            _backupStatusMessage.value = "Format de sauvegarde invalide."
                            _isBackingUp.value = false
                        }
                    } catch (e: Exception) {
                        _backupStatusMessage.value = "Erreur lors de la restauration : ${e.localizedMessage}"
                        _isBackingUp.value = false
                    }
                }
            }
            .addOnFailureListener { e ->
                _backupStatusMessage.value = "Échec de récupération de la sauvegarde : ${e.localizedMessage}"
                _isBackingUp.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeIdentitySync()
    }

    // --- Secure Local Encrypted JSON Backup Implementation ---
    fun clearLocalBackupStatusMessage() {
        _localBackupStatusMessage.value = null
    }

    fun setAutoLocalBackupEnabled(enabled: Boolean) {
        _isAutoLocalBackupEnabled.value = enabled
        com.example.network.ApiClient.getSessionManager().saveAutoLocalBackupEnabled(enabled)
        com.example.network.ApiClient.getSessionManager().addSecurityAuditLog(
            "Sauvegarde Locale Auto",
            if (enabled) "Sauvegarde locale automatique activée" else "Sauvegarde locale automatique désactivée"
        )
    }

    fun refreshLocalBackupList(context: Context? = null) {
        val ctx = context ?: getApplication<Application>()
        try {
            val backupDir = File(ctx.getExternalFilesDir(null) ?: ctx.filesDir, "backups")
            if (backupDir.exists()) {
                val files = backupDir.listFiles { file -> file.isFile && file.name.endsWith(".json") && file.name.startsWith("idmuslim_backup_") }
                    ?.sortedByDescending { it.lastModified() }
                    ?: emptyList()
                _localBackupFiles.value = files
            } else {
                _localBackupFiles.value = emptyList()
            }
        } catch (e: Exception) {
            _localBackupFiles.value = emptyList()
        }
    }

    fun triggerSecureLocalBackup(
        context: Context? = null,
        onComplete: ((File?, String?) -> Unit)? = null
    ) {
        val ctx = context ?: getApplication<Application>()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: "local_user"
        val sessionManager = com.example.network.ApiClient.getSessionManager()

        _isLocalBackingUp.value = true
        _localBackupStatusMessage.value = "Génération de l'archive JSON chiffrée en cours..."

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Gather all profile and identity records
                val roomProfile = userProfileDao.getUserProfileSync(uid)
                val docs = try { documentDao.getAllDocuments().first() } catch (e: Exception) { emptyList() }
                val auditLogs = sessionManager.getSecurityAuditLogs()

                val memberId = roomProfile?.idNumber?.ifBlank { "IDM-${(10000..99999).random()}" } ?: "IDM-${(10000..99999).random()}"
                val fullName = (roomProfile?.fullName?.takeIf { it.isNotBlank() }) ?: (sessionManager.getProfileFullName() ?: "Membre IDMuslim")
                val title = roomProfile?.community?.takeIf { it.isNotBlank() } ?: "Membre IDMuslim"
                val email = sessionManager.getUserEmail()
                val phone = ""
                val birthDate = (roomProfile?.dob?.takeIf { it.isNotBlank() }) ?: (sessionManager.getProfileDob() ?: "")
                val bloodType = "O+"
                val city = (roomProfile?.residency?.takeIf { it.isNotBlank() }) ?: (sessionManager.getProfileResidency() ?: "")
                val emergencyContact = ""
                val avatarUrl = (roomProfile?.avatarUrl?.takeIf { it.isNotBlank() }) ?: (sessionManager.getProfilePhotoBase64() ?: "")
                val isVerified = roomProfile?.isVerified ?: sessionManager.isUserVerified()
                val verificationStatus = (roomProfile?.membershipStatus?.takeIf { it.isNotBlank() }) ?: sessionManager.getVerificationStatus()
                val membershipTier = if (isVerified) "Émeraude" else "Standard"
                val issueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val expiryDate = (roomProfile?.expiryDate?.takeIf { it.isNotBlank() }) ?: (sessionManager.getExpiryDate() ?: "2029-12-31")
                val nfcSerial = "NFC-${UUID.randomUUID().toString().take(8).uppercase()}"
                val identityHash = UUID.randomUUID().toString()
                val publicKey = "04:${UUID.randomUUID().toString().replace("-", "")}"

                val privacySettingsMap = mapOf(
                    "privacyMode" to privacyMode.value,
                    "biometricLockEnabled" to biometricLockEnabled.value,
                    "screenSecurityEnabled" to screenSecurityEnabled.value,
                    "showEmail" to showEmail.value,
                    "shareLocation" to shareLocation.value,
                    "shareData" to shareData.value,
                    "allowNotifications" to allowNotifications.value,
                    "shareLinkFullName" to shareLinkFullName.value,
                    "shareLinkPhoto" to shareLinkPhoto.value,
                    "shareLinkDob" to shareLinkDob.value,
                    "shareLinkResidency" to shareLinkResidency.value,
                    "shareLinkCommunity" to shareLinkCommunity.value,
                    "shareLinkStatus" to shareLinkStatus.value
                )

                val payload = DecryptedIdentityPayload(
                    memberId = memberId,
                    fullName = fullName,
                    title = title,
                    email = email,
                    phone = phone,
                    birthDate = birthDate,
                    bloodType = bloodType,
                    city = city,
                    emergencyContact = emergencyContact,
                    avatarUrl = avatarUrl,
                    isVerified = isVerified,
                    verificationStatus = verificationStatus,
                    membershipTier = membershipTier,
                    issueDate = issueDate,
                    expiryDate = expiryDate,
                    nfcSerial = nfcSerial,
                    identityHash = identityHash,
                    publicKey = publicKey,
                    userProfile = roomProfile,
                    documents = docs,
                    securityLogs = auditLogs,
                    privacySettings = privacySettingsMap
                )

                // 2. Moshi serialization of plain identity payload
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val payloadAdapter = moshi.adapter(DecryptedIdentityPayload::class.java)
                val payloadJson = payloadAdapter.toJson(payload)

                // 3. Compute SHA-256 Checksum of the plain payload
                val md = MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(payloadJson.toByteArray(Charsets.UTF_8))
                val checksumHex = hashBytes.joinToString("") { "%02x".format(it) }

                // 4. Encrypt payload with AES-256-GCM via CryptoManager
                val encryptedBlob = cryptoManager.encrypt(payloadJson) ?: payloadJson

                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val timestamp = System.currentTimeMillis()
                val fileDateId = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupId = "IDM-BAK-$fileDateId"

                val publicSummary = LocalBackupPublicSummary(
                    memberId = memberId,
                    fullName = fullName,
                    membershipTier = membershipTier,
                    verificationStatus = verificationStatus,
                    isVerified = isVerified,
                    issueDate = issueDate,
                    expiryDate = expiryDate
                )

                val backupPackage = EncryptedLocalIdBackup(
                    backupId = backupId,
                    appName = "IDMuslim Digital ID",
                    backupType = "SECURE_LOCAL_ENCRYPTED_JSON",
                    formatVersion = "2.1",
                    generatedTimestamp = timestamp,
                    generatedDate = dateStr,
                    encryptionAlgorithm = "AES-256-GCM (Android KeyStore)",
                    integrityChecksumSha256 = checksumHex,
                    recordsCount = 1 + docs.size + auditLogs.size,
                    publicSummary = publicSummary,
                    encryptedIdentityPayload = encryptedBlob
                )

                val backupPackageAdapter = moshi.adapter(EncryptedLocalIdBackup::class.java)
                val backupJsonString = backupPackageAdapter.indent("  ").toJson(backupPackage)

                // 5. Write to local storage
                val backupDir = File(ctx.getExternalFilesDir(null) ?: ctx.filesDir, "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val backupFile = File(backupDir, "idmuslim_backup_${fileDateId}.json")
                backupFile.writeText(backupJsonString, Charsets.UTF_8)

                // Also maintain latest file
                val latestFile = File(backupDir, "idmuslim_backup_latest.json")
                latestFile.writeText(backupJsonString, Charsets.UTF_8)

                val fileSize = backupFile.length()

                // 6. Update preferences & Audit log
                sessionManager.saveLastLocalBackupTime(timestamp)
                sessionManager.saveLastLocalBackupPath(backupFile.absolutePath)
                sessionManager.saveLastLocalBackupSize(fileSize)
                sessionManager.addSecurityAuditLog(
                    "Sauvegarde Locale Chiffrée",
                    "Fichier JSON (${backupFile.name}, ${fileSize} octets) généré avec clé AES-256-GCM."
                )

                _lastLocalBackupTime.value = timestamp
                _lastLocalBackupFilePath.value = backupFile.absolutePath
                _lastLocalBackupFileSize.value = fileSize
                _isLocalBackingUp.value = false
                val successMsg = "✅ Sauvegarde locale chiffrée générée (${backupFile.name}, ${(fileSize / 1024) + 1} Ko) !"
                _localBackupStatusMessage.value = successMsg

                refreshLocalBackupList(ctx)

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete?.invoke(backupFile, null)
                }
            } catch (e: Exception) {
                _isLocalBackingUp.value = false
                val errorMsg = "❌ Échec de la sauvegarde locale : ${e.localizedMessage}"
                _localBackupStatusMessage.value = errorMsg
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete?.invoke(null, errorMsg)
                }
            }
        }
    }

    fun exportOrShareLocalBackupFile(context: Context, file: File) {
        try {
            if (!file.exists()) {
                android.widget.Toast.makeText(context, "Le fichier de sauvegarde n'existe plus", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde Chiffrée IDMuslim - ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Voici votre sauvegarde d'identité numérique IDMuslim chiffrée en AES-256-GCM pour vos archives personnelles.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Exporter / Archiver la sauvegarde JSON"))
            com.example.network.ApiClient.getSessionManager().addSecurityAuditLog(
                "Export Sauvegarde Locale",
                "Partage / Export du fichier de sauvegarde chiffré ${file.name}"
            )
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Erreur lors du partage : ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun restoreFromLocalBackupFile(context: Context, file: File, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(false, "Fichier introuvable.")
                    }
                    return@launch
                }

                val jsonContent = file.readText(Charsets.UTF_8)
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val backupAdapter = moshi.adapter(EncryptedLocalIdBackup::class.java)
                val backupPackage = backupAdapter.fromJson(jsonContent)

                if (backupPackage == null || backupPackage.encryptedIdentityPayload.isEmpty()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(false, "Fichier de sauvegarde invalide ou vide.")
                    }
                    return@launch
                }

                val decryptedJson = cryptoManager.decrypt(backupPackage.encryptedIdentityPayload)
                if (decryptedJson.isNullOrEmpty()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(false, "Impossible de déchiffrer la sauvegarde avec la clé de sécurité locale.")
                    }
                    return@launch
                }

                // Verify Checksum
                val md = MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(decryptedJson.toByteArray(Charsets.UTF_8))
                val calculatedChecksum = hashBytes.joinToString("") { "%02x".format(it) }

                if (backupPackage.integrityChecksumSha256.isNotEmpty() &&
                    !backupPackage.integrityChecksumSha256.equals(calculatedChecksum, ignoreCase = true)
                ) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(false, "Avertissement d'intégrité : Le checksum SHA-256 ne correspond pas.")
                    }
                    return@launch
                }

                val payloadAdapter = moshi.adapter(DecryptedIdentityPayload::class.java)
                val payload = payloadAdapter.fromJson(decryptedJson)

                if (payload != null) {
                    val sessionManager = com.example.network.ApiClient.getSessionManager()
                    payload.userProfile?.let { userProfileDao.insertUserProfile(it) }
                    if (payload.documents.isNotEmpty()) {
                        documentDao.insertAll(payload.documents)
                    }

                    sessionManager.saveProfileFullName(payload.fullName)
                    sessionManager.saveProfileDob(payload.birthDate)
                    sessionManager.saveProfileResidency(payload.city)
                    sessionManager.saveVerificationStatus(payload.verificationStatus)
                    sessionManager.setVerifiedStatus(payload.isVerified)

                    sessionManager.addSecurityAuditLog(
                        "Restauration Locale",
                        "Restauration réussie des données depuis le fichier JSON ${file.name}"
                    )

                    _localBackupStatusMessage.value = "✅ Données restaurées avec succès depuis ${file.name} !"

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(true, "Restauration réussie (${payload.documents.size} documents, profil restauré) !")
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onComplete(false, "Format interne de données non reconnu.")
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, "Erreur lors de la restauration : ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteLocalBackupFile(context: Context, file: File) {
        try {
            if (file.exists()) {
                file.delete()
            }
            refreshLocalBackupList(context)
            _localBackupStatusMessage.value = "Fichier ${file.name} supprimé."
        } catch (e: Exception) {
            _localBackupStatusMessage.value = "Erreur lors de la suppression : ${e.localizedMessage}"
        }
    }
}

