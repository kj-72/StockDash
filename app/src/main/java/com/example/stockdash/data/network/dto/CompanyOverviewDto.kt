package com.example.stockdash.data.network.dto

import com.example.stockdash.data.model.StockDetail
import com.squareup.moshi.Json

data class CompanyOverviewDto(
    @Json(name = "Symbol") val symbol: String?,
    @Json(name = "AssetType") val assetType: String?,
    @Json(name = "Name") val name: String?,
    @Json(name = "Description") val description: String?,

    @Json(name = "CIK") val cik: String?,
    @Json(name = "Exchange") val exchange: String?,
    @Json(name = "Currency") val currency: String?,
    @Json(name = "Country") val country: String?,
    @Json(name = "Sector") val sector: String?,
    @Json(name = "Industry") val industry: String?,
    @Json(name = "Address") val address: String?,

    @Json(name = "Beta") val beta: String?,
    @Json(name = "MarketCapitalization") val marketCap: String?,
    @Json(name = "PERatio") val peRatio: String?,
    @Json(name = "PEGRatio") val pegRatio: String?,
    @Json(name = "DividendYield") val dividendYield: String?,
    @Json(name = "ProfitMargin") val profitMargin: String?,
    @Json(name = "RevenueTTM") val revenueTTM: String?,
    @Json(name = "GrossProfitTTM") val grossProfitTTM: String?,
    @Json(name = "52WeekHigh") val yearHigh: String?,
    @Json(name = "52WeekLow") val yearLow: String?,
    @Json(name = "ExDividendDate") val exDividendDate: String?,
    @Json(name = "DividendDate") val dividendDate: String?,

    // To catch API limit messages from Alpha Vantage (these can appear at the top level)
    @Json(name = "Information") val apiInformation: String? = null,
    @Json(name = "Note") val apiNote: String? = null

) {
    fun toStockDetail(): StockDetail {
        return StockDetail(
            symbol = symbol ?: "",
            name = name ?: "",
            marketCap = marketCap ?: "",
            peRatio = peRatio ?: "",
            pegRatio = pegRatio ?: "",
            dividendYield = dividendYield ?: "",
            profitMargin = profitMargin ?: "",
            revenueTTM = revenueTTM ?: "",
            grossProfitTTM = grossProfitTTM ?: "",
            yearHigh = yearHigh ?: "",
            yearLow = yearLow ?: "",
            exDividendDate = exDividendDate ?: "",
            dividendDate = dividendDate ?: "",
            cik = cik ?: "",
            exchange = exchange ?: "",
            currency = currency ?: "",
            sector = sector ?: "",
            country = country ?: "",
            industry = industry ?: "",
            address = address ?: "",
            assetType = assetType ?: "",
            description = description ?: "",
            beta = beta ?: "",
        )
    }
}
