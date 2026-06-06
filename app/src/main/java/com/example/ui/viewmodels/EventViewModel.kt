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

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
    }

    private val _isUserVerified = MutableStateFlow(com.example.network.ApiClient.getSessionManager().isUserVerified())
    val isUserVerified: StateFlow<Boolean> = _isUserVerified.asStateFlow()

    private val _profilePhotoBase64 = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getProfilePhotoBase64())
    val profilePhotoBase64: StateFlow<String?> = _profilePhotoBase64.asStateFlow()

    private val _cardTheme = MutableStateFlow(com.example.network.ApiClient.getSessionManager().getCardTheme())
    val cardTheme: StateFlow<Int> = _cardTheme.asStateFlow()

    fun verifyIdentity(success: Boolean = true) {
        com.example.network.ApiClient.getSessionManager().setVerifiedStatus(success)
        _isUserVerified.value = success
    }

    fun updateProfilePhoto(base64: String) {
        com.example.network.ApiClient.getSessionManager().saveProfilePhotoBase64(base64)
        _profilePhotoBase64.value = base64
    }

    fun updateCardTheme(themeIndex: Int) {
        com.example.network.ApiClient.getSessionManager().saveCardTheme(themeIndex)
        _cardTheme.value = themeIndex
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
