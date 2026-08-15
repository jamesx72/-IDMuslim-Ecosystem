package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MosqueSearchRepository(private val dao: MosqueSearchDao) {

    val recentSearches: Flow<List<MosqueSearchEntity>> = dao.getRecentSearches(limit = 5)

    suspend fun saveSearchQuery(rawQuery: String, resultCount: Int) = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return@withContext

        // Remove old matching query if exists so new timestamp takes precedence
        dao.deleteSearchByQuery(trimmed)
        // Insert newly updated search record
        dao.insertOrUpdateSearch(
            MosqueSearchEntity(
                query = trimmed,
                timestamp = System.currentTimeMillis(),
                resultCount = resultCount
            )
        )
        // Keep only the most recent 5 records safely
        val all = dao.getAllSearchesList()
        if (all.size > 5) {
            all.drop(5).forEach { extra ->
                dao.deleteSearchById(extra.id)
            }
        }
    }

    suspend fun removeSearch(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSearchById(id)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }
}
