package com.example.stockdash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stockdash.data.local.entity.CachedStockDetailEntity

@Dao
interface StockDetailDao {
    @Query("SELECT * FROM cached_stock_details WHERE symbol = :symbol AND fetchDate = :fetchDate")
    suspend fun getCachedStockDetail(symbol: String, fetchDate: String): CachedStockDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockDetail(data: CachedStockDetailEntity)

    @Query("DELETE FROM cached_stock_details WHERE symbol = :symbol")
    suspend fun clearStockDetailCacheForSymbol(symbol: String)
}