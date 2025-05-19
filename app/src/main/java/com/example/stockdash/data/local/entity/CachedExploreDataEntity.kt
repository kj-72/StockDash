package com.example.stockdash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stockdash.data.model.ExploreScreenData
import com.example.stockdash.data.model.ExploreStockInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType

@Entity(tableName = "cached_explore_data")
data class CachedExploreDataEntity(
    @PrimaryKey val id: Int = 0, // Single row for this type of cached data
    val topGainersJson: String,    // Store complex lists as JSON strings
    val topLosersJson: String,
    val mostActivelyTradedJson: String,
    val fetchDate: String// Store date as ISO string, e.g., "2023-10-28"
) {
    // Mapper to domain model (can also be an extension function outside)
    fun toExploreScreenData(moshi: Moshi): ExploreScreenData {
        val stockInfoListType = newParameterizedType(List::class.java, ExploreStockInfo::class.java)
        val stockInfoListAdapter = moshi.adapter<List<ExploreStockInfo>>(stockInfoListType)

        return ExploreScreenData(
            topGainers = stockInfoListAdapter.fromJson(topGainersJson) ?: emptyList(),
            topLosers = stockInfoListAdapter.fromJson(topLosersJson) ?: emptyList(),
            mostActivelyTraded = stockInfoListAdapter.fromJson(mostActivelyTradedJson) ?: emptyList()
        )
    }
}
