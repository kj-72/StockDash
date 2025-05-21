package com.example.stockdash.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) // Enables Moshi to convert to JSON and back
data class ChartDataPoint(
    val date: String,
    val price: Float
)
