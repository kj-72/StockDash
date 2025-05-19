package com.example.stockdash.data.network.dto

import com.squareup.moshi.Json

data class SearchStockItemDto(
    @Json(name = "1. symbol") val symbol: String?,
    @Json(name = "2. name") val name: String?,
    @Json(name = "3. type") val type: String?,
    @Json(name = "4. region") val region: String?,
    @Json(name = "8. currency") val currency: String?,
)
