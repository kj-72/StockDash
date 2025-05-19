package com.example.stockdash.data.model

import com.example.stockdash.data.local.entity.CachedExploreDataEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType

data class ExploreScreenData(
    val topGainers: List<ExploreStockInfo>,
    val topLosers: List<ExploreStockInfo>,
    val mostActivelyTraded: List<ExploreStockInfo>) {


    fun toCacheEntity(date: String, moshi: Moshi): CachedExploreDataEntity {
        val stockInfoListType = newParameterizedType(List::class.java, ExploreStockInfo::class.java)
        val stockInfoListAdapter = moshi.adapter<List<ExploreStockInfo>>(stockInfoListType)

        return CachedExploreDataEntity(
            topGainersJson = stockInfoListAdapter.toJson(this.topGainers),
            topLosersJson = stockInfoListAdapter.toJson(this.topLosers),
            mostActivelyTradedJson = stockInfoListAdapter.toJson(this.mostActivelyTraded),
            fetchDate = date
        )
    }
}
