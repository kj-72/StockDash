package com.example.stockdash.data.model

import com.example.stockdash.data.local.entity.RecentSearchEntity

data class RecentSearch(
    val symbol: String, // The search term itself
    val name: String,
    val type: String,
    val timestamp: Long // When it was searched, for ordering
)
