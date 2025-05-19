package com.example.stockdash.di

import android.content.Context
import androidx.room.Room
import com.example.stockdash.data.local.AppDatabase
import com.example.stockdash.data.local.dao.ChartDataDao
import com.example.stockdash.data.local.dao.ExploreDataDao
import com.example.stockdash.data.local.dao.RecentSearchDao
import com.example.stockdash.data.local.dao.StockDetailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "stock_dash_db"
        ).build()
    }

    @Provides
    @Singleton // DAOs are usually fine as singletons if DB is singleton
    fun provideRecentSearchDao(appDatabase: AppDatabase): RecentSearchDao {
        return appDatabase.recentSearchDao()
    }

    @Provides
    @Singleton
    fun provideExploreDataDao(appDatabase: AppDatabase): ExploreDataDao { // Provide new DAO
        return appDatabase.exploreDataDao()
    }

    @Provides
    @Singleton
    fun provideChartDataDao(appDatabase: AppDatabase): ChartDataDao { // Provide new DAO
        return appDatabase.chartDataDao()
    }

    @Provides
    @Singleton
    fun provideStockDetailDao(appDatabase: AppDatabase): StockDetailDao {
        return appDatabase.stockDetailDao() // Get it from your AppDatabase instance
    }
}