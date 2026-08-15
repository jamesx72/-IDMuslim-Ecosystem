package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MosqueSearchDao {
    @Query("SELECT * FROM mosque_search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 5): Flow<List<MosqueSearchEntity>>

    @Query("SELECT * FROM mosque_search_history ORDER BY timestamp DESC")
    suspend fun getAllSearchesList(): List<MosqueSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSearch(search: MosqueSearchEntity): Long

    @Query("DELETE FROM mosque_search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM mosque_search_history WHERE query = :query")
    suspend fun deleteSearchByQuery(query: String)

    @Query("DELETE FROM mosque_search_history")
    suspend fun clearHistory()
}
