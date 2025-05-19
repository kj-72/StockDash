package com.example.stockdash.data.network.dto

import com.example.stockdash.data.model.ChartDataPoint
import com.example.stockdash.data.model.TimeSeriesData
import com.squareup.moshi.Json

data class TimeSeriesMonthlyDto(
    @Json(name = "Monthly Adjusted Time Series")
    val timeSeriesData: Map<String, TimeSeriesAdjustedEntryDto>?,

    @Json(name = "Information") val apiInformation: String? = null,
    @Json(name = "Note") val apiNote: String? = null) {

    fun toTimeSeriesData(): TimeSeriesData {
        val points = timeSeriesData?.map { ChartDataPoint(it.key, it.value.close.toFloatOrNull() ?: 0f) } ?: emptyList()
        return TimeSeriesData(points)
    }
}
