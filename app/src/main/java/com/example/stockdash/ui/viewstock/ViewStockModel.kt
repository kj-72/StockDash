package com.example.stockdash.ui.viewstock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdash.data.model.StockDetail
import com.example.stockdash.data.model.TimeSeriesData
import com.example.stockdash.data.repository.StockRepository
import com.example.stockdash.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewStockModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val savedStateHandle: SavedStateHandle // For navigation arguments
) : ViewModel() {

    // Assuming "symbolArg" is the name of the argument in your nav_graph.xml
    private val stockSymbol =
        MutableStateFlow(savedStateHandle.get<String>(ViewStockFragment.Companion.ARG_SYMBOL))
    private var interval = "1D"

    private val _stockDetailState = MutableStateFlow<Resource<StockDetail>>(Resource.Loading())
    val stockDetailState: StateFlow<Resource<StockDetail>> = _stockDetailState.asStateFlow()

    private val _timeSeriesDataState =
        MutableStateFlow<Resource<TimeSeriesData>>(Resource.Loading())
    val timeSeriesDataState: StateFlow<Resource<TimeSeriesData>> = _timeSeriesDataState.asStateFlow()

    init {
        stockSymbol.value?.let { symbol ->
            if (symbol.isNotBlank()) {
                fetchStockDetails(symbol, interval)
            } else {
                _stockDetailState.value = Resource.Error("No stock symbol provided.")
            }
        } ?: run {
            _stockDetailState.value = Resource.Error("Stock symbol not found in arguments.")
        }
    }

    fun updateInterval(newInterval: String) {
        val currentSymbol = stockSymbol.value
        if (currentSymbol.isNullOrBlank()) {
            _timeSeriesDataState.value = Resource.Error("Cannot update interval without a valid symbol.")
            return
        }

        interval = newInterval
        viewModelScope.launch {
            _timeSeriesDataState.value = Resource.Loading()
            stockRepository.getStockCharts(currentSymbol, newInterval).collectLatest { result ->
                _timeSeriesDataState.value = result
            }
        }
    }

    fun fetchStockDetails(symbol: String, interval: String) {
        // (symbol update)
        if (stockSymbol.value != symbol) stockSymbol.value = symbol

        // Fetch Stock Details
        viewModelScope.launch {
            _stockDetailState.value = Resource.Loading()
            stockRepository.getStockDetails(symbol).collectLatest { result ->
                _stockDetailState.value = result
            }
        }
        // Fetch Time Series Data (for a default interval, e.g., "1D")
        viewModelScope.launch {
            _timeSeriesDataState.value = Resource.Loading()
            stockRepository.getStockCharts(symbol, interval).collectLatest { result ->
                _timeSeriesDataState.value = result
            }
        }
    }
}