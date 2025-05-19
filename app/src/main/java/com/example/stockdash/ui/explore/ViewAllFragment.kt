package com.example.stockdash.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stockdash.data.model.ExploreStockInfo
import com.example.stockdash.databinding.FragmentViewAllBinding
import com.example.stockdash.ui.MainViewModel
import com.example.stockdash.ui.explore.adapter.ExploreStockViewAdapter
import com.example.stockdash.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewAllFragment : Fragment() {

    private lateinit var binding: FragmentViewAllBinding
    private val exploreViewModel: ExploreViewModel by activityViewModels() // Shared ViewModel
    private val mainViewModel: MainViewModel by activityViewModels() // Shared ViewModel

    private lateinit var viewAllAdapter: ExploreStockViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewAllAdapter = ExploreStockViewAdapter { stockInfo ->
            openViewProductFragment(stockInfo)
        }
        binding.recyclerViewViewAll.apply {
            adapter = viewAllAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        observeView()
    }

    private fun observeView() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                exploreViewModel.filteredViewAllList.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            viewAllAdapter.showUnloadedItems(8)
                        }
                        is Resource.Success -> {
                            viewAllAdapter.submitList(resource.data)
                            val viewType = exploreViewModel.getCurrentViewAllType()
                            mainViewModel.setUiStateForViewAll(viewType)
                        }
                        is Resource.Error -> {
                            binding.recyclerViewViewAll.visibility = View.GONE
                            viewAllAdapter.submitList(emptyList())

                        }
                    }
                }
            }
        }
    }

    private fun openViewProductFragment(stock: ExploreStockInfo) {
        mainViewModel.userClickedStockInExplore(stock)
    }


}