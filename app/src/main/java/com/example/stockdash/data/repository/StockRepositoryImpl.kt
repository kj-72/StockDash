package com.example.stockdash.data.repository

import com.example.stockdash.data.network.dto.TimeSeries60MinDto
import com.example.stockdash.data.network.dto.TimeSeriesDailyDto
import com.example.stockdash.data.network.dto.TimeSeriesWeeklyDto
import android.util.Log
import com.example.stockdash.data.local.dao.ChartDataDao
import com.example.stockdash.data.local.dao.ExploreDataDao
import com.example.stockdash.data.local.dao.RecentSearchDao
import com.example.stockdash.data.local.dao.StockDetailDao
import com.example.stockdash.data.local.entity.RecentSearchEntity
import com.example.stockdash.data.model.ExploreScreenData
import com.example.stockdash.data.model.RecentSearch
import com.example.stockdash.data.model.SearchStockInfo
import com.example.stockdash.data.model.StockDetail
import com.example.stockdash.data.model.TimeSeriesData
import com.example.stockdash.data.network.AlphaVantageApiService
import com.example.stockdash.data.network.dto.TimeSeries15MinDto
import com.example.stockdash.data.network.dto.TimeSeriesMonthlyDto
import com.example.stockdash.util.Resource
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Tells Hilt this should be a singleton (one instance for the app)
class StockRepositoryImpl @Inject constructor(
    private val apiService: AlphaVantageApiService, // Hilt will inject this
    private val recentSearchDao: RecentSearchDao,
    private val exploreDataDao: ExploreDataDao,
    private val chartDataDao: ChartDataDao,
    private val stockDetailDao: StockDetailDao,
    private val moshi: Moshi
) : StockRepository {

    override suspend fun getStockDetails(symbol: String): Flow<Resource<StockDetail>> {
        return flow {
            emit(Resource.Loading())
            val todayDateString = LocalDate.now().toString() // Or kotlinx.datetime equivalent
            val cleanSymbol = symbol.trim().uppercase()

            // 1. Try to fetch from cache
            val cachedEntity = withContext(Dispatchers.IO) {
                stockDetailDao.getCachedStockDetail(cleanSymbol, todayDateString)
            }
            if (cachedEntity != null) {
                Log.d("StockRepository", "Serving StockDetail for $cleanSymbol from cache for date: $todayDateString")
                emit(Resource.Success(cachedEntity.toStockDetail()))
                return@flow
            }

            // Call the generic handler
            val result = safeApiCall(
                apiCall = { apiService.getCompanyOverview(symbol) },
                mapToDomain = { companyOverviewDto -> companyOverviewDto.toStockDetail() }
            )
            when (result) {
                is Resource.Success -> {
                    result.data?.let { domainStockDetail ->
                        // Ensure symbol is not null before creating entity
                        if (domainStockDetail.symbol != null) {
                            withContext(Dispatchers.IO) {
                                stockDetailDao.clearStockDetailCacheForSymbol(domainStockDetail.symbol)
                                stockDetailDao.insertStockDetail(
                                    domainStockDetail.toCacheEntity(todayDateString) // Moshi not needed if direct field mapping
                                )
                            }
                            emit(Resource.Success(domainStockDetail))
                        } else {
                            emit(Resource.Error("Fetched stock detail has null symbol."))
                        }
                    } ?: emit(Resource.Error("Network data for StockDetail was null."))
                }
                is Resource.Error -> emit(result)

                is Resource.Loading -> emit(result)
            }

        }
    }


    override suspend fun getExploreScreenData(): Flow<Resource<ExploreScreenData>> {
        return flow {
            emit(Resource.Loading())
            val todayDateString = LocalDate.now().toString()

            // 1. Try to fetch from cache
            val cachedEntity = withContext(Dispatchers.IO) {
                exploreDataDao.getCachedExploreData()
            }

            if (cachedEntity != null && cachedEntity.fetchDate == todayDateString) {
                Log.d("StockRepository", "Serving ExploreScreenData from cache for date: $todayDateString")
                // Moshi instance is needed by toDomainModel if it's defined on the entity
                emit(Resource.Success(cachedEntity.toExploreScreenData(moshi)))
                return@flow // Data is fresh from cache, no need to fetch from network
            }

            // Call the generic handler
            val result = safeApiCall(
                apiCall = { apiService.getTopMovers() },
                mapToDomain = { topMoversDto ->  topMoversDto.toExploreScreenData() }
            )
            when (result) {
                is Resource.Success -> {
                        result.data?.let { domainData ->
                        // Save to cache
                        withContext(Dispatchers.IO) {
                            exploreDataDao.insertExploreData(domainData.toCacheEntity(todayDateString, moshi))
                            Log.d("StockRepository", "ExploreScreenData saved to cache for date: $todayDateString")
                        }
                        emit(Resource.Success(domainData))
                    } ?: emit(Resource.Error("Network data was null after successful call."))
                }
                is Resource.Error -> {
                    // If network fails, but we had some (stale) cached data, we could emit that as a fallback
                    if (cachedEntity != null) {
                        Log.w("StockRepository", "Network failed, serving stale cache: ${result.message}")
                        emit(Resource.Success(cachedEntity.toExploreScreenData(moshi))) // Emit stale cache with original error
                        // Or emit Resource.Error(networkResultResource.message, cachedEntity.toDomainModel(moshi))
                    } else {
                        emit(Resource.Error(result.message ?: "Failed to fetch explore data."))
                    }
                }
                is Resource.Loading -> { emit(result) }
            }
        }
    }

    override suspend fun getStockCharts(symbol: String, interval: String): Flow<Resource<TimeSeriesData>> {
        return flow {
            emit(Resource.Loading())

            val todayDateString = LocalDate.now().toString()
            val cleanSymbol = symbol.trim().uppercase()

            val cachedEntity = withContext(Dispatchers.IO) {
                chartDataDao.getCachedChartData(cleanSymbol, interval ,todayDateString)
            }

            if (cachedEntity != null) {
                Log.d("StockRepository", "Serving $interval chart data for $cleanSymbol from cache for date: $todayDateString")
                emit(Resource.Success(cachedEntity.toTimeSeriesData(moshi)))
                return@flow
            }

            // Call the generic handler

            val result = when (interval) {
                "1D" -> safeApiCall(
                    apiCall = { apiService.getTimeSeries15Min(symbol ) },
                    mapToDomain = { timeSeriesEntryDto -> timeSeriesEntryDto.toTimeSeriesData() },
                )
                "1W" -> safeApiCall(
                    apiCall = { apiService.getTimeSeries60Min(symbol ) },
                    mapToDomain = { timeSeriesEntryDto -> timeSeriesEntryDto.toTimeSeriesData() },
                )
                "1M" -> safeApiCall(
                    apiCall = { apiService.getTimeSeriesDaily(symbol) },
                    mapToDomain = { timeSeriesEntryDto -> timeSeriesEntryDto.toTimeSeriesData() },
                )
                "3M", "1Y" -> safeApiCall(
                    apiCall = { apiService.getTimeSeriesWeekly(symbol) },
                    mapToDomain = { timeSeriesEntryDto -> timeSeriesEntryDto.toTimeSeriesData() },
                )
                "5Y" -> safeApiCall(
                    apiCall = { apiService.getTimeSeriesMonthly(symbol) },
                    mapToDomain = { timeSeriesEntryDto -> timeSeriesEntryDto.toTimeSeriesData() },
                )
                else -> Resource.Error("Invalid interval")
            }

            when (result) {
                is Resource.Success -> {
                    result.data?.let { domainData ->
                        withContext(Dispatchers.IO) {
                            chartDataDao.clearChartDataForSymbolAndInterval(cleanSymbol, interval) // clear old cache

                            chartDataDao.insertChartData(domainData.toCacheEntity(cleanSymbol, interval, todayDateString, moshi)) // Enter fresh cache
                        }
                        emit(Resource.Success(domainData))
                    } ?: emit(Resource.Error("Network data for chart was null after successful call."))
                }
                is Resource.Error -> {
                    emit(result)
                }
                is Resource.Loading -> { emit(result) }
            }
        }
    }

    // Other interface methods will be implemented later
    override suspend fun searchStock(query: String, type: String): Flow<Resource<List<SearchStockInfo>>> {
        return flow {
            emit(Resource.Loading())

            val result = safeApiCall(
                apiCall = { apiService.searchStock(query) },
                mapToDomain = { searchResultDto -> searchResultDto.toSearchStockInfo(type) },
            )
            emit(result)
        }
    }


    override fun getRecentSearches(): Flow<List<RecentSearch>> {

        return recentSearchDao.getRecentSearches(limit = 16).map { entityList -> // Get top 16 searches
            entityList.map { entity -> entity.toRecentSearch() } // Map each entity in the list
        }
    }

    override suspend fun addRecentSearch(stock: SearchStockInfo) {
        withContext(Dispatchers.IO) {
            val recentSearchEntity = RecentSearchEntity(
                symbol = stock.symbol?:"",
                name = stock.name?:"",
                type = stock.type?:"",
                timestamp = System.currentTimeMillis()
            )
            recentSearchDao.insertOrUpdateSearch(recentSearchEntity)
            recentSearchDao.trimOldSearches(keepCount = 20) // Keep only the top 20 most recent
        }
    }

    override suspend fun updateRecentSearch(query: String) {
        withContext(Dispatchers.IO) {
            recentSearchDao.updateSearch(query)
        }
    }

    override suspend fun deleteRecentSearch(recentSearch: RecentSearch) {
        withContext(Dispatchers.IO) {
            recentSearchDao.deleteBySymbol(recentSearch.symbol)
        }
    }

    private suspend fun <ApiDto, DomainModel> safeApiCall(
        apiCall: suspend () -> Response<ApiDto>,
        mapToDomain: (ApiDto) -> DomainModel,
    ): Resource<DomainModel> {
        try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

                // Check for Alpha Vantage API limit messages using the provided lambda or type check
                val apiNote: String? = getApiNoteFromDto(responseBody)

                if (apiNote != null ) {
                    return Resource.Error(apiNote) // Return Resource.Error directly
                }

                return Resource.Success(mapToDomain(responseBody)) // Return Resource.Success
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message() ?: "Unknown API error"
                return Resource.Error("API Error: ${response.code()} - $errorMsg")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return Resource.Error("Network error: Could not connect. Please check your internet connection.")
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("An unexpected error occurred: ${e.localizedMessage}")
        }
    }

    private fun <T> getApiNoteFromDto(dto: T): String? {
        return when (dto) {
            is TimeSeries15MinDto -> dto.apiInformation ?: dto.apiNote
            is TimeSeries60MinDto -> dto.apiInformation ?: dto.apiNote
            is TimeSeriesDailyDto -> dto.apiInformation ?: dto.apiNote
            is TimeSeriesWeeklyDto -> dto.apiInformation ?: dto.apiNote
            is TimeSeriesMonthlyDto -> dto.apiInformation ?: dto.apiNote
            else -> null
        }
    }
}
