package com.example.stockdash.data.local.dao

import androidx.room.*
import com.example.stockdash.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    // Get top N recent searches, ordered by most recent
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 16): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // If searched again, update timestamp
    suspend fun insertOrUpdateSearch(search: RecentSearchEntity)

    @Query("UPDATE recent_searches SET timestamp = :timestamp WHERE symbol = :symbol")
    suspend fun updateSearch(symbol: String, timestamp: Long = System.currentTimeMillis())

    // To automatically trim older searches beyond the limit
    @Query("DELETE FROM recent_searches WHERE symbol NOT IN (SELECT symbol FROM recent_searches ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun trimOldSearches(keepCount: Int = 20)

    // Clear all recent searches (No use yet)
    @Query("DELETE FROM recent_searches")
    suspend fun clearAllRecentSearches()

    @Query("DELETE FROM recent_searches WHERE symbol = :symbolToSearch")
    suspend fun deleteBySymbol(symbolToSearch: String)
}