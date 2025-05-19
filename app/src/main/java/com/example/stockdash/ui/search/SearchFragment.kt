package com.example.stockdash.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockdash.R
import com.example.stockdash.ui.search.adapter.RecentSearchAdapter
import com.example.stockdash.ui.search.adapter.SearchViewAdapter
import com.example.stockdash.data.model.RecentSearch
import com.example.stockdash.data.model.SearchStockInfo
import com.example.stockdash.databinding.FragmentSearchBinding
import com.example.stockdash.ui.MainViewModel
import com.example.stockdash.util.Resource
import com.example.stockdash.util.resolveThemeColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchViewAdapter: SearchViewAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    private var currentDisplayState = DisplayState.SHOWING_RECENT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeSearchResult()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setUiStateForSearchActive()
    }


    private fun observeSearchResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { // Observe new search
                    mainViewModel.searchResults.collectLatest { result ->
                        Log.d("TAG", result.toString() + "  " + result.data.toString())
                        when (result) {
                            is Resource.Loading -> {
                                showSearchResultsLoading()
                            }
                            is Resource.Success -> {
                                if (result.data == null){
                                    Toast.makeText(requireContext(), "Search Result NULL on Success", Toast.LENGTH_LONG).show()
                                    return@collectLatest
                                }
                                if (result.data.isEmpty() && mainViewModel.currentSearchInput.value.isNotEmpty()) {
                                    showNoResultView(mainViewModel.filteredRecentSearches.value)
                                }
                                else if (result.data.isNotEmpty()) {
                                    showSearchResultsView(result.data)
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                launch {
                    mainViewModel.filteredRecentSearches.collectLatest { recentList ->
                        if (binding.recyclerViewRecentSearches.isVisible) {
                            showRecentSearchesView(recentList)
                        }
                    }
                }
            }
        }
    }

    private fun setupView() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.recyclerViewSearchResult.isVisible) {
                        mainViewModel.clearSearch()
                        showRecentSearchesView(mainViewModel.filteredRecentSearches.value)
                    }
                    else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        mainViewModel.onSearchVisibilityChanged(false)

        searchViewAdapter = SearchViewAdapter { stockInfo ->
            mainViewModel.userSelectedNewSearch(stockInfo)
        }
        binding.recyclerViewSearchResult.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSearchResult.adapter = searchViewAdapter

        recentSearchAdapter = RecentSearchAdapter(
            onItemClicked = { recentSearch ->
                mainViewModel.recentSearchItemSelected(recentSearch)
                recentSearch.symbol.let { mainViewModel.userSelectedRecentSearch(it) }
            },
            onItemRemoved = { recentSearch ->
                mainViewModel.deleteRecentSearch(recentSearch)
            }
        )
        binding.recyclerViewRecentSearches.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecentSearches.adapter = recentSearchAdapter

        binding.cardAll.setOnClickListener {
            changeFilter(resources.getString(R.string.all))
        }
        binding.cardEtf.setOnClickListener {
            changeFilter(resources.getString(R.string.etf))
        }
        binding.cardEquity.setOnClickListener {
            changeFilter(resources.getString(R.string.equity))
        }
        binding.cardMutualFund.setOnClickListener {
            changeFilter(resources.getString(R.string.mutual_fund))
        }
    }

    private fun showSearchResultsView(results: List<SearchStockInfo>) {
        mainViewModel.onSearchVisibilityChanged(true)

        binding.recyclerViewRecentSearches.visibility = View.GONE
        binding.textViewNoResult.visibility = View.GONE
        binding.textViewSearchHeading.text = resources.getString(R.string.search_results_heading)

        binding.recyclerViewSearchResult.visibility = View.VISIBLE
        currentDisplayState = DisplayState.SHOWING_RESULTS
        searchViewAdapter.submitList(results)
    }

    private fun showRecentSearchesView(recentSearches: List<RecentSearch>) {
        mainViewModel.onSearchVisibilityChanged(false)

        binding.recyclerViewSearchResult.visibility = View.GONE
        binding.textViewNoResult.visibility = View.GONE
        binding.textViewSearchHeading.text = resources.getString(R.string.recent_search_heading)

        binding.recyclerViewRecentSearches.visibility = View.VISIBLE
        currentDisplayState = DisplayState.SHOWING_RECENT
        recentSearchAdapter.submitList(recentSearches)
    }

    private fun showNoResultView(recentSearches: List<RecentSearch>) {
        mainViewModel.onSearchVisibilityChanged(true)

        binding.recyclerViewSearchResult.visibility = View.GONE
        binding.textViewSearchHeading.text = resources.getString(R.string.recent_search_heading)

        binding.textViewNoResult.text = resources.getString(R.string.no_result)
        binding.textViewNoResult.visibility = View.VISIBLE
        binding.recyclerViewRecentSearches.visibility = View.VISIBLE
        currentDisplayState = DisplayState.SHOWING_RECENT
        recentSearchAdapter.submitList(recentSearches)
    }

    private fun showSearchResultsLoading() {
        binding.recyclerViewSearchResult.visibility = View.VISIBLE
        binding.textViewSearchHeading.text = resources.getString(R.string.search_results_heading)
        binding.textViewNoResult.visibility = View.GONE
        binding.recyclerViewRecentSearches.visibility = View.GONE
        currentDisplayState = DisplayState.SHOWING_RESULTS
        searchViewAdapter.showLoading()
    }

    private fun changeFilter(filter: String) {
        val oldFilter = if (mainViewModel.isSearchResultVisible.value)
            mainViewModel.currentSearchFilter.value
        else
            mainViewModel.currentRecentSearchFilter.value

        if (oldFilter == filter) return

        val selectedColor = context?.resolveThemeColor(R.attr.filterChipSelectedBackgroundColor)?: return
        val unselectedColor = context?.resolveThemeColor(R.attr.filterChipUnselectedBackgroundColor)?:return

        when (oldFilter) {
            resources.getString(R.string.all) -> binding.cardAll.setCardBackgroundColor(unselectedColor)
            resources.getString(R.string.etf) -> binding.cardEtf.setCardBackgroundColor(unselectedColor)
            resources.getString(R.string.equity) -> binding.cardEquity.setCardBackgroundColor(unselectedColor)
            resources.getString(R.string.mutual_fund) -> binding.cardMutualFund.setCardBackgroundColor(unselectedColor)
        }
        mainViewModel.updateSearchFilter(filter)
        when (filter) {
            resources.getString(R.string.all) -> binding.cardAll.setCardBackgroundColor(selectedColor)
            resources.getString(R.string.etf) -> binding.cardEtf.setCardBackgroundColor(selectedColor)
            resources.getString(R.string.equity) -> binding.cardEquity.setCardBackgroundColor(selectedColor)
            resources.getString(R.string.mutual_fund) -> binding.cardMutualFund.setCardBackgroundColor(selectedColor)
        }
    }

    private enum class DisplayState {
        SHOWING_RESULTS,
        SHOWING_RECENT
    }

}