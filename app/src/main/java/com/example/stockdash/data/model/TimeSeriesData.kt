package com.example.stockdash.data.model

import com.example.stockdash.data.local.entity.CachedChartDataEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

data class TimeSeriesData(val data: List<ChartDataPoint>) {

    fun toCacheEntity(symbol: String, intervalRange: String, fetchDate: String, moshi: Moshi): CachedChartDataEntity {
        val listType = Types.newParameterizedType(List::class.java, ChartDataPoint::class.java)
        val adapter = moshi.adapter<List<ChartDataPoint>>(listType)
        return CachedChartDataEntity(
            symbol = symbol,
            intervalRange = intervalRange,
            fetchDate = fetchDate,
            chartDataJson = adapter.toJson(this.data)
        )
    }
}
