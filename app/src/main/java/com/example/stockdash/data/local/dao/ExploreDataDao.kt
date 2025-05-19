package com.example.stockdash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stockdash.data.local.entity.CachedExploreDataEntity

@Dao
interface ExploreDataDao {
    @Query("SELECT * FROM cached_explore_data WHERE id = 0") // Assuming single row with id 0
    suspend fun getCachedExploreData(): CachedExploreDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExploreData(data: CachedExploreDataEntity)

    @Query("DELETE FROM cached_explore_data WHERE id = 0") // For clearing old cache
    suspend fun clearCache()
}