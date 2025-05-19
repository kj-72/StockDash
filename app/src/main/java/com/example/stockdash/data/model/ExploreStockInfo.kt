package com.example.stockdash.data.model

data class ExploreStockInfo (
    val ticker: String?,
    val price: Double?,
    val priceChange: Double?,
    val priceChangePercentage: Double?,
    val volume: Double?,
)