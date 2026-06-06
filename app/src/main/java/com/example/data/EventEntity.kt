package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val price: Double = 0.0,
    val maxTickets: Int = 0,
    val availableTickets: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val memberId: String,
    val scanCode: String,
    val status: String // "Valid", "Used", "Cancelled"
)

@Entity(tableName = "waitlist")
data class WaitlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int,
    val memberId: String,
    val timestamp: Long = System.currentTimeMillis()
)
