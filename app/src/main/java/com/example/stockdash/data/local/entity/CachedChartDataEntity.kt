package com.example.stockdash.data.local.entity

import androidx.room.Entity
import com.example.stockdash.data.model.ChartDataPoint
import com.example.stockdash.data.model.TimeSeriesData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

// Composite Primary Key: symbol, interval (representing the UI range like "1D"), and fetchDate
@Entity(tableName = "cached_chart_data", primaryKeys = ["symbol", "intervalRange", "fetchDate"])
data class CachedChartDataEntity(
    val symbol: String,
    val intervalRange: String, // "1D", "1W", "1M", "3M", "1Y", "5Y"
    val fetchDate: String,     // ISO date string "YYYY-MM-DD or YYYY-MM-DD HH:MM:SS"
    val chartDataJson: String  // Store List<Pair<String, Float>> as JSON string for Moshi
) {
    // Mapper to domain model
    fun toTimeSeriesData(moshi: Moshi): TimeSeriesData {
        val chartPointType = Types.newParameterizedType(ChartDataPoint::class.java, String::class.java, Float::class.javaObjectType)
        val listType = Types.newParameterizedType(List::class.java, chartPointType)
        val adapter = moshi.adapter<List<ChartDataPoint>>(listType)
        return TimeSeriesData(data = adapter.fromJson(chartDataJson) ?: emptyList())
    }
}
