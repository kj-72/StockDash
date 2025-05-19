package com.example.stockdash.data.network

import com.example.stockdash.data.network.dto.TimeSeries60MinDto
import com.example.stockdash.data.network.dto.TimeSeriesDailyDto
import com.example.stockdash.data.network.dto.TimeSeriesWeeklyDto
import com.example.stockdash.data.network.dto.TopMoversResponseDto
import com.example.stockdash.data.network.dto.CompanyOverviewDto
import com.example.stockdash.data.network.dto.SearchStockResponseDto
import com.example.stockdash.data.network.dto.TimeSeries15MinDto
import com.example.stockdash.data.network.dto.TimeSeriesMonthlyDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface AlphaVantageApiService {
    companion object {
        private const val ARG_15_MIN = "15min"
        private const val ARG_60_MIN = "60min"
    }

    @GET("query?function=TOP_GAINERS_LOSERS")
    suspend fun getTopMovers(
        @Query("apikey") apiKey: String
    ): Response<TopMoversResponseDto>

    @GET("query?function=SYMBOL_SEARCH")
    suspend fun searchStock(
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): Response<SearchStockResponseDto>

    @GET("query?function=OVERVIEW")
    suspend fun getCompanyOverview(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<CompanyOverviewDto>

    @GET("query?function=TIME_SERIES_INTRADAY")
    suspend fun getTimeSeries15Min(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("interval") interval: String = ARG_15_MIN
    ): Response<TimeSeries15MinDto>

    @GET("query?function=TIME_SERIES_INTRADAY")
    suspend fun getTimeSeries60Min(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("interval") interval: String = ARG_60_MIN
    ): Response<TimeSeries60MinDto>

    @GET("query?function=TIME_SERIES_DAILY") // Adjusted Daily is premium?
    suspend fun getTimeSeriesDaily(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<TimeSeriesDailyDto>

    @GET("query?function=TIME_SERIES_WEEKLY_ADJUSTED")
    suspend fun getTimeSeriesWeekly(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<TimeSeriesWeeklyDto>

    @GET("query?function=TIME_SERIES_MONTHLY_ADJUSTED")
    suspend fun getTimeSeriesMonthly(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<TimeSeriesMonthlyDto>

}