package com.example.stockdash.data.model

import com.example.stockdash.data.local.entity.CachedStockDetailEntity

data class StockDetail(
    val symbol: String?,
    val name: String?,
    val assetType: String?,
    val description: String?,

    val beta: String?,
    val cik: String?,
    val exchange: String?,
    val currency: String?,
    val sector: String?,
    val country: String?,
    val industry: String?,
    val address: String?,

    val marketCap: String?,
    val peRatio: String?,
    val pegRatio: String?,
    val dividendYield: String?,
    val profitMargin: String?,
    val revenueTTM: String?,
    val grossProfitTTM: String?,
    val yearHigh: String?,
    val yearLow: String?,
    val exDividendDate: String?,
    val dividendDate: String?) {

    fun toCacheEntity(fetchDate: String): CachedStockDetailEntity {
        return CachedStockDetailEntity(
            symbol = symbol ?: "",
            fetchDate = fetchDate,
            name = name,
            assetType = assetType,
            description = description,
            beta = beta,
            cik = cik,
            exchange = exchange,
            currency = currency,
            sector = sector,
            country = country,
            industry = industry,
            address = address,
            marketCap = marketCap,
            peRatio = peRatio,
            pegRatio = pegRatio,
            dividendYield = dividendYield,
            profitMargin = profitMargin,
            revenueTTM = revenueTTM,
            grossProfitTTM = grossProfitTTM,
            yearHigh = yearHigh,
            yearLow = yearLow,
            exDividendDate = exDividendDate,
            dividendDate = dividendDate
        )
    }
}
