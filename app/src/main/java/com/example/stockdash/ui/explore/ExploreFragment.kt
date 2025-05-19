package com.example.stockdash.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockdash.ui.explore.adapter.ExploreStockViewAdapter
import com.example.stockdash.ui.explore.adapter.ExploreRecentSearchAdapter
import com.example.stockdash.data.model.ExploreStockInfo
import com.example.stockdash.data.model.RecentSearch
import com.example.stockdash.databinding.FragmentExploreBinding
import com.example.stockdash.ui.MainViewModel
import com.example.stockdash.ui.ViewAllArg
import com.example.stockdash.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.stockdash.ui.ViewAllArg.MOST_TRADED
import com.example.stockdash.ui.ViewAllArg.TOP_GAINERS
import com.example.stockdash.ui.ViewAllArg.TOP_LOSERS

@AndroidEntryPoint
class ExploreFragment : Fragment() {
    private lateinit var binding: FragmentExploreBinding
    private val exploreViewModel: ExploreViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val exploreItemsCount = 4

    private lateinit var gainersAdapter: ExploreStockViewAdapter
    private lateinit var losersAdapter: ExploreStockViewAdapter
    private lateinit var mostTradedAdapter: ExploreStockViewAdapter
    private lateinit var recentlySearchedAdapter: ExploreRecentSearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExploreBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        observeExploreData()
    }


    private fun setupView() {
        // For Recently Searched
        recentlySearchedAdapter = ExploreRecentSearchAdapter { stock ->
            mainViewModel.userSelectedRecentSearch(stock.symbol)
        }
        binding.recyclerViewRecentlySearched.apply {
            adapter = recentlySearchedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // For Gainers
        gainersAdapter = ExploreStockViewAdapter { stockInfo ->
            openViewProductFragment(stockInfo)
        }
        binding.recyclerViewTopGainers.apply {
            adapter = gainersAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        // For Losers
        losersAdapter = ExploreStockViewAdapter { stockInfo ->
            openViewProductFragment(stockInfo)
        }
        binding.recyclerViewTopLosers.apply {
            adapter = losersAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        // For Most Traded
        mostTradedAdapter = ExploreStockViewAdapter { stockInfo ->
            openViewProductFragment(stockInfo)
        }
        binding.recyclerViewMostTraded.apply { // Assuming activeRecyclerView is the ID for most traded
            adapter = mostTradedAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        // For Settins
        binding.cardViewSettings.setOnClickListener {
            mainViewModel.userClickedOnSettings()
        }
    }

    private fun observeExploreData() {
        // Use viewLifecycleOwner.lifecycleScope for Fragment coroutines tied to view's lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle ensures collection starts when STARTED and stops when STOPPED
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    exploreViewModel.exploreScreenDataState.collectLatest { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                gainersAdapter.showUnloadedItems(exploreItemsCount)
                                losersAdapter.showUnloadedItems(exploreItemsCount)
                                mostTradedAdapter.showUnloadedItems(exploreItemsCount)
                            }

                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    gainersAdapter.submitList(data.topGainers.take(4))
                                    losersAdapter.submitList(data.topLosers.take(4))
                                    mostTradedAdapter.submitList(data.mostActivelyTraded.take(4))

                                } ?: run {
                                    Toast.makeText(requireContext(), "Data NULL even on success", Toast.LENGTH_SHORT).show()
                                    return@collectLatest
                                }
                                setupViewAllButtons()
                            }

                            is Resource.Error -> {
                                removeRecyclerViews()
                                Toast.makeText(context, resource.message ?: "Resource.Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                launch {
                    mainViewModel.rawRecentSearches.collectLatest { searches ->
                        setupRecentSearches(searches)
                    }
                }
            }
        }
    }

    private fun setupViewAllButtons() {
        binding.viewAllTopGainers.setOnClickListener {
            openViewAllFragment(TOP_GAINERS)
        }
        binding.viewAllTopLosers.setOnClickListener {
            openViewAllFragment(TOP_LOSERS)
        }
        binding.viewAllMostTraded.setOnClickListener {
            openViewAllFragment(MOST_TRADED)
        }
    }
    private fun removeRecyclerViews() {
        binding.recyclerViewTopGainers.visibility = View.GONE
        binding.recyclerViewTopLosers.visibility = View.GONE
        binding.recyclerViewMostTraded.visibility = View.GONE
    }

    private fun setupRecentSearches(recentlySearched: List<RecentSearch>) {
        if (recentlySearched.isNotEmpty()) {
            binding.textViewRecentlySearched.visibility = View.VISIBLE
            binding.viewAllRecentlySearched.visibility = View.VISIBLE
            binding.cardViewRecentlySearched.visibility = View.VISIBLE
            recentlySearchedAdapter.submitList(recentlySearched.take(3))

        } else {
            binding.textViewRecentlySearched.visibility = View.GONE
            binding.viewAllRecentlySearched.visibility = View.GONE
            binding.cardViewRecentlySearched.visibility = View.GONE
        }
    }

    private fun openViewProductFragment(stock: ExploreStockInfo) {
        mainViewModel.userClickedStockInExplore(stock)
    }

    private fun openViewAllFragment(type: ViewAllArg) {
        exploreViewModel.updateViewAllType(type)
        mainViewModel.userClickedViewAllInExplore(type)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setUiStateForExplore()
    }


}