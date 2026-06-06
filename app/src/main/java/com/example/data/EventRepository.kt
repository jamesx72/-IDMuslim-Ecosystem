package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventById(id: Int): EventEntity? {
        return eventDao.getEventById(id)
    }

    suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)
    }

    fun getTicketsForEvent(eventId: Int): Flow<List<TicketEntity>> {
        return eventDao.getTicketsForEvent(eventId)
    }
    
    fun getTicketsForMember(memberId: String): Flow<List<TicketEntity>> {
        return eventDao.getTicketsForMember(memberId)
    }

    suspend fun registerForEvent(ticket: TicketEntity): Boolean {
        return eventDao.registerForEvent(ticket)
    }

    suspend fun joinWaitlist(eventId: Int, memberId: String): Boolean {
        val existing = eventDao.getWaitlistEntryForMember(eventId, memberId)
        if (existing == null) {
            eventDao.insertWaitlistEntry(WaitlistEntity(eventId = eventId, memberId = memberId))
            return true
        }
        return false
    }

    suspend fun cancelTicketAndProcessWaitlist(ticket: TicketEntity): WaitlistEntity? {
        eventDao.cancelTicketStatus(ticket.id)
        eventDao.incrementAvailableTickets(ticket.eventId)

        // Check waitlist
        val nextInLine = eventDao.getNextWaitlistEntry(ticket.eventId)
        if (nextInLine != null) {
            // Automatically register from waitlist
            val newTicket = TicketEntity(
                eventId = ticket.eventId,
                memberId = nextInLine.memberId,
                scanCode = java.util.UUID.randomUUID().toString(),
                status = "Valid"
            )
            eventDao.registerForEvent(newTicket)
            eventDao.removeWaitlistEntry(nextInLine.id)
            return nextInLine
        }
        return null
    }

    suspend fun getWaitlistEntryForMember(eventId: Int, memberId: String): WaitlistEntity? {
        return eventDao.getWaitlistEntryForMember(eventId, memberId)
    }
}
