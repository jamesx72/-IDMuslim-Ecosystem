package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPlaceDao {
    @Query("SELECT * FROM cached_places ORDER BY name ASC")
    fun getAllCachedPlaces(): Flow<List<CachedPlaceEntity>>

    @Query("SELECT * FROM cached_places ORDER BY name ASC")
    suspend fun getAllCachedPlacesList(): List<CachedPlaceEntity>

    @Query("SELECT * FROM cached_places WHERE type = :type ORDER BY name ASC")
    fun getCachedPlacesByType(type: String): Flow<List<CachedPlaceEntity>>

    @Query("SELECT * FROM cached_places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: String): CachedPlaceEntity?

    @Query("SELECT * FROM cached_places WHERE eventId = :eventId LIMIT 1")
    suspend fun getPlaceByEventId(eventId: Int): CachedPlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<CachedPlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: CachedPlaceEntity)

    @Query("DELETE FROM cached_places WHERE id = :id")
    suspend fun deletePlaceById(id: String)

    @Query("DELETE FROM cached_places WHERE lastCachedTimestamp < :olderThanTimestamp")
    suspend fun deleteOldCache(olderThanTimestamp: Long)

    @Query("DELETE FROM cached_places")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_places")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM cached_places")
    suspend fun getPlaceCount(): Int
}
