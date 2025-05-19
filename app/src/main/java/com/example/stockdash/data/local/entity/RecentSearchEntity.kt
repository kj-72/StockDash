package com.example.stockdash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stockdash.data.model.RecentSearch

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val symbol: String, // The search symbol which was opened
    val name: String?,
    val type: String?,// Optional: "Stock", "ETF"
    val timestamp: Long // When it was searched, for ordering
) {
    fun toRecentSearch(): RecentSearch {
        return RecentSearch(
            symbol = this.symbol,
            name = this.name?:"",
            type = this.type?:"",
            timestamp = this.timestamp
        )
    }
}
