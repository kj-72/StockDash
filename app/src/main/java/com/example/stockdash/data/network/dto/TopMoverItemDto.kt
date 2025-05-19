package com.example.stockdash.data.network.dto

import com.example.stockdash.data.model.ExploreStockInfo
import com.squareup.moshi.Json

data class TopMoverItemDto(
    @Json(name = "ticker") val ticker: String,
    @Json(name = "price") val price: String,
    @Json(name = "change_amount") val changeAmount: String,
    @Json(name = "change_percentage") val changePercentage: String,
    @Json(name = "volume") val volume: String) {

    fun toStockInfo(): ExploreStockInfo {
        return ExploreStockInfo(
            ticker = this.ticker,
            price = this.price.toDoubleOrNull(),
            priceChange = this.changeAmount.toDoubleOrNull(),
            priceChangePercentage = this.changePercentage.substring(0, this.changePercentage.length - 1).toDoubleOrNull(),
            volume = this.volume.toDoubleOrNull()
        )
    }
}