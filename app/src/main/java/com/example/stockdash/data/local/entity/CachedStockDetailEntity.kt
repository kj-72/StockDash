package com.example.stockdash.data.local.entity

import androidx.room.Entity
import com.example.stockdash.data.model.StockDetail

@Entity(tableName = "cached_stock_details", primaryKeys = ["symbol", "fetchDate"])
data class CachedStockDetailEntity(
    val symbol: String, // Non-null in entity as it's part of primary key
    val fetchDate: String,

    // Fields corresponding to StockDetail domain model
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


    fun toStockDetail(): StockDetail {
        return StockDetail(
            symbol = this.symbol,
            name = this.name,
            assetType = this.assetType,
            description = this.description,
            beta = this.beta,
            cik = this.cik,
            exchange = this.exchange,
            currency = this.currency,
            sector = this.sector,
            country = this.country,
            industry = this.industry,
            address = this.address,
            marketCap = this.marketCap,
            peRatio = this.peRatio,
            pegRatio = this.pegRatio,
            dividendYield = this.dividendYield,
            profitMargin = this.profitMargin,
            revenueTTM = this.revenueTTM,
            grossProfitTTM = this.grossProfitTTM,
            yearHigh = this.yearHigh,
            yearLow = this.yearLow,
            exDividendDate = this.exDividendDate,
            dividendDate = this.dividendDate
        )
    }
}

