package com.example.stockdash.data.network.dto

import com.example.stockdash.data.model.ExploreScreenData
import com.squareup.moshi.Json

data class TopMoversResponseDto(
    @Json(name = "metadata") val metadata: String?,
    @Json(name = "last_updated") val lastUpdated: String?,
    @Json(name = "top_gainers") val topGainers: List<TopMoverItemDto>?,
    @Json(name = "top_losers") val topLosers: List<TopMoverItemDto>?,
    @Json(name = "most_actively_traded") val mostActivelyTraded: List<TopMoverItemDto>?,

    // To catch API limit messages from Alpha Vantage (these can appear at the top level)
    @Json(name = "Information") val apiInformation: String? = null,
    @Json(name = "Note") val apiNote: String? = null
) {
    fun toExploreScreenData() = ExploreScreenData(
        topGainers = topGainers?.map { it.toStockInfo() } ?: emptyList(),
        topLosers = topLosers?.map { it.toStockInfo() } ?: emptyList(),
        mostActivelyTraded = mostActivelyTraded?.map { it.toStockInfo() } ?: emptyList()
    )
}
