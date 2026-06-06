package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Query("SELECT * FROM tickets WHERE eventId = :eventId")
    fun getTicketsForEvent(eventId: Int): Flow<List<TicketEntity>>
    
    @Query("SELECT * FROM tickets WHERE memberId = :memberId")
    fun getTicketsForMember(memberId: String): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("UPDATE events SET availableTickets = availableTickets - 1 WHERE id = :eventId AND availableTickets > 0")
    suspend fun decrementAvailableTickets(eventId: Int): Int // returns number of rows updated

    @Transaction
    suspend fun registerForEvent(ticket: TicketEntity): Boolean {
        val updatedRows = decrementAvailableTickets(ticket.eventId)
        if (updatedRows > 0) {
            insertTicket(ticket)
            return true
        }
        return false
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaitlistEntry(waitlist: WaitlistEntity)

    @Query("SELECT * FROM waitlist WHERE eventId = :eventId ORDER BY timestamp ASC LIMIT 1")
    suspend fun getNextWaitlistEntry(eventId: Int): WaitlistEntity?

    @Query("SELECT * FROM waitlist WHERE eventId = :eventId ORDER BY timestamp ASC")
    fun getWaitlistFlow(eventId: Int): Flow<List<WaitlistEntity>>

    @Query("DELETE FROM waitlist WHERE id = :id")
    suspend fun removeWaitlistEntry(id: Int)

    @Query("SELECT * FROM waitlist WHERE eventId = :eventId AND memberId = :memberId LIMIT 1")
    suspend fun getWaitlistEntryForMember(eventId: Int, memberId: String): WaitlistEntity?

    @Query("UPDATE tickets SET status = 'Cancelled' WHERE id = :ticketId")
    suspend fun cancelTicketStatus(ticketId: Int)

    @Query("UPDATE events SET availableTickets = availableTickets + 1 WHERE id = :eventId")
    suspend fun incrementAvailableTickets(eventId: Int)
}
