package com.example.stockdash.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdash.data.model.ExploreScreenData
import com.example.stockdash.data.model.ExploreStockInfo
import com.example.stockdash.data.repository.StockRepository
import com.example.stockdash.ui.ViewAllArg
import com.example.stockdash.ui.ViewAllArg.TOP_GAINERS
import com.example.stockdash.ui.ViewAllArg.MOST_TRADED
import com.example.stockdash.ui.ViewAllArg.TOP_LOSERS
import com.example.stockdash.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val stockRepository: StockRepository // Hilt injects the implementation via RepositoryModule
) : ViewModel() {

    // StateFlow to hold the data for the Explore screen (Top Gainers, Losers, Active)
    private val _exploreScreenDataState = MutableStateFlow<Resource<ExploreScreenData>>(Resource.Loading())
    val exploreScreenDataState: StateFlow<Resource<ExploreScreenData>> = _exploreScreenDataState.asStateFlow()

    // StateFlow to hold the type of view all (Top Gainers, Losers, Active)
    private val _currentViewAllType = MutableStateFlow(TOP_GAINERS)
    val filteredViewAllList: StateFlow<Resource<List<ExploreStockInfo>>> =
        combine(_exploreScreenDataState, _currentViewAllType) { exploreResource, viewAllType ->
            when (exploreResource) {
                is Resource.Loading -> Resource.Loading()
                is Resource.Success -> {
                    val exploreData = exploreResource.data
                    if (exploreData == null) {
                        Resource.Error("Explore data is null")
                    }
                    else {
                        val selectedList = when (viewAllType) {
                            TOP_GAINERS -> exploreData.topGainers
                            TOP_LOSERS -> exploreData.topLosers
                            MOST_TRADED -> exploreData.mostActivelyTraded
                        }
                        Resource.Success(selectedList)
                    }
                }
                is Resource.Error -> Resource.Error(exploreResource.message ?: "Failed to load explore data")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading() // Initial state for the filtered list
        )

    init {
        fetchExploreData() // Fetch data when the ViewModel is created
    }

    private fun fetchExploreData() {
        viewModelScope.launch {
            // Set to loading before starting the fetch, in case it's a refresh
            _exploreScreenDataState.value = Resource.Loading()
            stockRepository.getExploreScreenData().collectLatest { result ->
                _exploreScreenDataState.value = result
            }
        }
    }

    fun getCurrentViewAllType() = _currentViewAllType.value
    fun updateViewAllType(arg: ViewAllArg) {
        _currentViewAllType.value = arg
    }
}