package com.example.stockdash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stockdash.data.local.dao.ChartDataDao
import com.example.stockdash.data.local.dao.ExploreDataDao
import com.example.stockdash.data.local.dao.RecentSearchDao
import com.example.stockdash.data.local.dao.StockDetailDao
import com.example.stockdash.data.local.entity.CachedChartDataEntity
import com.example.stockdash.data.local.entity.CachedExploreDataEntity
import com.example.stockdash.data.local.entity.CachedStockDetailEntity
import com.example.stockdash.data.local.entity.RecentSearchEntity

@Database(
    entities = [RecentSearchEntity::class, CachedExploreDataEntity::class, CachedChartDataEntity::class, CachedStockDetailEntity::class],
    version = 1,
    exportSchema = false
)
// Database for the app
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun exploreDataDao(): ExploreDataDao
    abstract fun chartDataDao(): ChartDataDao
    abstract fun stockDetailDao(): StockDetailDao
}