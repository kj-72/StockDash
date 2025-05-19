package com.example.stockdash.data.network.dto

import com.example.stockdash.data.model.SearchStockInfo
import com.squareup.moshi.Json

data class SearchStockResponseDto (
    @Json(name = "bestMatches") val bestMatches: List<SearchStockItemDto>?,

    @Json(name = "Information") val apiInformation: String? = null,
    @Json(name = "Note") val apiNote: String? = null
) {
    fun toSearchStockInfo(type: String): List<SearchStockInfo> {
        if (bestMatches == null) return emptyList()
        val returnList = mutableListOf<SearchStockInfo>()
        val isFiltering = type != "All"
        for (item in bestMatches) {
            if (isFiltering && item.type != type) continue

            returnList.add(
                SearchStockInfo(
                    symbol = item.symbol,
                    name = item.name,
                    type = item.type,
                    region = item.region,
                    currency = item.currency
                )
            )
        }
        return returnList
    }

}