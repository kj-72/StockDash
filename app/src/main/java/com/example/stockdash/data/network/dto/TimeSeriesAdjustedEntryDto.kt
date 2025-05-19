package com.example.stockdash.data.network.dto

import com.squareup.moshi.Json

data class TimeSeriesAdjustedEntryDto(
    @Json(name = "1. open") val open: String,
    @Json(name = "2. high") val high: String,
    @Json(name = "3. low") val low: String,
    @Json(name = "5. adjusted close") val close: String
)
