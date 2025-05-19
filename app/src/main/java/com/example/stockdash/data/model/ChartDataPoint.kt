package com.example.stockdash.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) // Helps Moshi with Kotlin classes
data class ChartDataPoint(
    val date: String, // Or Long if you convert to timestamp before serialization
    val price: Float
)
