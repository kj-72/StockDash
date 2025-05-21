package com.example.stockdash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdash.data.model.ExploreStockInfo
import com.example.stockdash.data.model.RecentSearch
import com.example.stockdash.data.model.SearchStockInfo
import com.example.stockdash.data.repository.StockRepository
import com.example.stockdash.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.stockdash.ui.ViewAllArg.TOP_GAINERS
import com.example.stockdash.ui.ViewAllArg.TOP_LOSERS
import com.example.stockdash.ui.ViewAllArg.MOST_TRADED


enum class MainActivityUiState {
    EXPLORE,            // Shows app name + search bar
    SEARCH_ACTIVE,      // Hides app name, search bar focuses
    SETTINGS,           // Shows "Settings" title, hides search bar
    VIEW_ALL_GAINERS,   // Shows "Top Gainers" title, hides search bar
    VIEW_ALL_LOSERS,    // Shows "Top Losers" title, hides search bar
    VIEW_ALL_MOST_TRADED, // Shows "Most Traded" title, hides search bar
    VIEW_STOCK      // Hides app name and search bar (or custom title for product)
}

enum class ViewAllArg {
    TOP_GAINERS, TOP_LOSERS, MOST_TRADED
}

sealed class NavigationEvent {
    data class ToProductDetail(val symbol: String) : NavigationEvent()
    data class ToViewAll(val listType: ViewAllArg) : NavigationEvent()
    object ToSettings : NavigationEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(private val stockRepository: StockRepository) : ViewModel() {

    // This will hold the text from the EditText, updated as the user types
    private val _currentSearchInput = MutableStateFlow("")
    val currentSearchInput: StateFlow<String> = _currentSearchInput.asStateFlow()

    // This will hold the search filter (e.g., "All", "ETFs", "Mutual Funds")
    private val _currentSearchResultFilter = MutableStateFlow("All")
    val currentSearchFilter: StateFlow<String> = _currentSearchResultFilter.asStateFlow()

    private val _currentRecentSearchFilter = MutableStateFlow("All")
    val currentRecentSearchFilter: StateFlow<String> = _currentRecentSearchFilter.asStateFlow()

    // This will hold the actual search results after the search button is clicked
    private var rawSearchResults: List<SearchStockInfo> = emptyList()
    private val _searchResults = MutableStateFlow<Resource<List<SearchStockInfo>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<SearchStockInfo>>> = _searchResults.asStateFlow()

    // To control visibility of SearchFragment
    private val _showSearchFragment = MutableStateFlow(false)
    val isSearchResultVisible: StateFlow<Boolean> = _showSearchFragment.asStateFlow()

    // StateFlow for MainActivity's UI configuration
    private val _mainActivityUiState = MutableStateFlow<MainActivityUiState>(MainActivityUiState.EXPLORE) // Default state
    val mainActivityUiState: StateFlow<MainActivityUiState> = _mainActivityUiState.asStateFlow()

    // Shared Flow for Navigation Events
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // --- For Recent Searches (SearchFragment will display these) ---
    val rawRecentSearches: StateFlow<List<RecentSearch>> = stockRepository.getRecentSearches().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredRecentSearches: StateFlow<List<RecentSearch>> =
        combine(rawRecentSearches, _currentRecentSearchFilter) { recent, filter ->
            applyFilterToRecentSearches(recent, filter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Called from MainActivity when EditText text changes, just to store the current input
    fun onSearchInputChanged(newInput: String) {
        _currentSearchInput.value = newInput
    }

    fun updateSearchFilter(newFilter: String) {
        if (_showSearchFragment.value) {
            _currentSearchResultFilter.value = newFilter
            _searchResults.value = Resource.Success(applyFilterToSearchResults(rawSearchResults, newFilter))
        }
        else {
            _currentRecentSearchFilter.value = newFilter
        }
    }


    private fun applyFilterToSearchResults(list: List<SearchStockInfo>, filter: String): List<SearchStockInfo> {
        if (filter == "All") {
            return list
        }
        return list.filter { stockInfo ->
            stockInfo.type?.equals(filter) == true
        }
    }

    private fun applyFilterToRecentSearches(list: List<RecentSearch>, filter: String): List<RecentSearch> {
        if (filter == "All") {
            return list
        }
        return list.filter { stockInfo ->
            stockInfo.type == filter
        }
    }

    fun onSearchVisibilityChanged(isVisible: Boolean) {
        _showSearchFragment.value = isVisible
    }

    // Called from MainActivity when the search button is clicked
    fun performSearch() {
        val query = _currentSearchInput.value.trim() // Get the current input and trim whitespace
        if (query.isBlank() || query.isEmpty()) { // Basic validation
            _searchResults.value = Resource.Error("Please enter a valid search term.")
            return
        }

        _showSearchFragment.value = true
        viewModelScope.launch {
            _searchResults.value = Resource.Loading()
            stockRepository.searchStock(query, _currentRecentSearchFilter.value).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> _searchResults.value = Resource.Loading()
                    is Resource.Success -> {
                        rawSearchResults = result.data ?: emptyList()
                        _searchResults.value = Resource.Success(applyFilterToSearchResults(rawSearchResults, _currentSearchResultFilter.value))
                        _showSearchFragment.value = !result.data.isNullOrEmpty()
                    }
                    is Resource.Error -> {
                        _searchResults.value = Resource.Error(result.message ?: "Unknown error")
                    }
                }
            }
        }
    }

    // Called from SearchFragment when a stock is selected from results
    fun userSelectedRecentSearch(symbol: String?) {
        viewModelScope.launch {
            symbol?.let { symbol -> // Use safe call
                if (symbol.isNotBlank()) {
                    viewModelScope.launch {
                        stockRepository.updateRecentSearch(symbol)
                        _navigationEvents.emit(NavigationEvent.ToProductDetail(symbol))
                    }
                }
            }
        }
    }
    fun userSelectedNewSearch(stockInfo: SearchStockInfo) {
        stockInfo.symbol?.let { symbol -> // Use safe call
            if (symbol.isNotBlank()) {
                viewModelScope.launch {
                    stockRepository.addRecentSearch(stockInfo)
                    _navigationEvents.emit(NavigationEvent.ToProductDetail(symbol))
                }
            }
        }
    }

    fun deleteRecentSearch(recentSearch: RecentSearch) {
        viewModelScope.launch {
            stockRepository.deleteRecentSearch(recentSearch)
        }
    }

    // Called from SearchFragment when a recent search item is selected
    fun recentSearchItemSelected(recentSearch: RecentSearch) {
        viewModelScope.launch {
            stockRepository.updateRecentSearch(recentSearch.symbol)
        }
    }

    // Call this if user explicitly clears the search (e.g., 'X' button in EditText)
    fun clearSearch() {
        _currentSearchInput.value = ""
        _searchResults.value = Resource.Success(emptyList())
    }

    fun setUiStateForExplore() {
        _mainActivityUiState.value = MainActivityUiState.EXPLORE
    }

    fun setUiStateForSearchActive() {
        _mainActivityUiState.value = MainActivityUiState.SEARCH_ACTIVE
    }

    fun setUiStateForSettings() {
        _mainActivityUiState.value = MainActivityUiState.SETTINGS
    }

    fun setUiStateForViewAll(listType: ViewAllArg) { // listType: "TOP_GAINERS", "TOP_LOSERS", etc.
        _mainActivityUiState.value = when (listType) {
            TOP_GAINERS -> MainActivityUiState.VIEW_ALL_GAINERS
            TOP_LOSERS -> MainActivityUiState.VIEW_ALL_LOSERS
            MOST_TRADED -> MainActivityUiState.VIEW_ALL_MOST_TRADED
        }
    }

    fun setUiStateForProductDetail() {
        _mainActivityUiState.value = MainActivityUiState.VIEW_STOCK
    }

    fun userClickedStockInExplore(stock: ExploreStockInfo) {
        viewModelScope.launch {
            stock.ticker?.let { _navigationEvents.emit(NavigationEvent.ToProductDetail(it)) }
        }
    }

    fun userClickedViewAllInExplore(listType: ViewAllArg) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToViewAll(listType))
        }
    }

    fun userClickedOnSettings() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.ToSettings)
        }
    }

}
