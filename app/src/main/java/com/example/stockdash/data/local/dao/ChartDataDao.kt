package com.example.stockdash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stockdash.data.local.entity.CachedChartDataEntity

@Dao
interface ChartDataDao {
    @Query("SELECT * FROM cached_chart_data WHERE symbol = :symbol AND intervalRange = :intervalRange AND fetchDate = :fetchDate")
    suspend fun getCachedChartData(symbol: String, intervalRange: String, fetchDate: String): CachedChartDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChartData(data: CachedChartDataEntity)


    @Query("DELETE FROM cached_chart_data WHERE symbol = :symbol AND intervalRange = :intervalRange")
    suspend fun clearChartDataForSymbolAndInterval(symbol: String, intervalRange: String)

}