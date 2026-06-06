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
}
