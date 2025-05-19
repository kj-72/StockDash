package com.example.stockdash.data.repository

import com.example.stockdash.data.model.ExploreScreenData
import com.example.stockdash.data.model.RecentSearch
import com.example.stockdash.data.model.SearchStockInfo
import com.example.stockdash.data.model.StockDetail
import com.example.stockdash.data.model.TimeSeriesData
import com.example.stockdash.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getExploreScreenData(): Flow<Resource<ExploreScreenData>>

    suspend fun searchStock(query: String, type: String): Flow<Resource<List<SearchStockInfo>>>

    suspend fun getStockDetails(symbol: String): Flow<Resource<StockDetail>>

    suspend fun getStockCharts(symbol: String, interval: String): Flow<Resource<TimeSeriesData>>

    fun getRecentSearches(): Flow<List<RecentSearch>> // For ROOM

    suspend fun addRecentSearch(stock: SearchStockInfo)

    suspend fun updateRecentSearch(query: String)

    suspend fun deleteRecentSearch(recentSearch: RecentSearch)
}