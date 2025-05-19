package com.example.stockdash

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stockdash.databinding.ActivityMainBinding
import com.example.stockdash.ui.MainActivityUiState
import com.example.stockdash.ui.MainActivityUiState.EXPLORE
import com.example.stockdash.ui.MainActivityUiState.SEARCH_ACTIVE
import com.example.stockdash.ui.MainActivityUiState.SETTINGS
import com.example.stockdash.ui.MainActivityUiState.VIEW_ALL_GAINERS
import com.example.stockdash.ui.MainActivityUiState.VIEW_ALL_LOSERS
import com.example.stockdash.ui.MainActivityUiState.VIEW_ALL_MOST_TRADED
import com.example.stockdash.ui.MainActivityUiState.VIEW_STOCK
import com.example.stockdash.ui.MainViewModel
import com.example.stockdash.ui.NavigationEvent
import com.example.stockdash.ui.ViewAllArg
import com.example.stockdash.ui.ViewAllArg.MOST_TRADED
import com.example.stockdash.ui.ViewAllArg.TOP_GAINERS
import com.example.stockdash.ui.ViewAllArg.TOP_LOSERS
import com.example.stockdash.ui.explore.ViewAllFragment
import com.example.stockdash.ui.search.SearchFragment
import com.example.stockdash.ui.settings.SettingsFragment
import com.example.stockdash.ui.viewstock.ViewStockFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeMainActivityUiState()
        observeNavigation()
        setupView()
    }

    private fun observeMainActivityUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainActivityUiState.collect { uiState -> // Change UI states based on current fragment
                    updateToolbarForState(uiState)
                }
            }
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { // Change Fragments navigation
                viewModel.navigationEvents.collectLatest { event ->
                    navigateTo(event)
                }
            }
        }
    }

    private fun setupView() {
        binding.editTextSearch.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                switchToSearchFragment() // Open Search Fragment after clicking on Search
                binding.appNameTextView.visibility = View.GONE
            }
        }

        // Perform search after clicking keyboard search button
        binding.editTextSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO) {
                viewModel.performSearch()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
        // Observe search input changes for View Model
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchInputChanged(s.toString()) // Update Input Value
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateToolbarForState(state: MainActivityUiState) {
        when (state) {
            EXPLORE -> resumeExploreFragment()
            SEARCH_ACTIVE -> resumeSearchFragment()
            SETTINGS -> resumeSettingsFragment()
            VIEW_ALL_GAINERS -> resumeViewAllFragment(TOP_GAINERS)
            VIEW_ALL_LOSERS -> resumeViewAllFragment(TOP_LOSERS)
            VIEW_ALL_MOST_TRADED -> resumeViewAllFragment(MOST_TRADED)
            VIEW_STOCK -> resumeViewProductFragment()
        }
    }

    private fun navigateTo(to: NavigationEvent) {
        when (to) {
            is NavigationEvent.ToProductDetail -> switchToViewStockFragment(to.symbol)
            is NavigationEvent.ToViewAll -> switchToViewAllFragment()
            is NavigationEvent.ToSettings -> switchToSettingsFragment()
        }
    }

    // Switch between fragments

    private fun switchToSearchFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, SearchFragment())
            addToBackStack(null)
            commit()
        }
    }
    private fun switchToViewAllFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, ViewAllFragment())
            addToBackStack(null)
            commit()
        }
    }
    private fun switchToSettingsFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, SettingsFragment())
            addToBackStack(null)
            commit()
        }
    }
    private fun switchToViewStockFragment(stockSymbol: String) {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, ViewStockFragment.newInstance(stockSymbol))
            addToBackStack(null)
            commit()
        }
    }

    private fun resumeSearchFragment() {
        binding.appNameTextView.visibility = View.GONE
        binding.editTextSearch.visibility = View.VISIBLE
    }
    private fun resumeSettingsFragment() {
        binding.appNameTextView.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.GONE
        binding.appNameTextView.text = getString(R.string.settings)
    }
    private fun resumeViewAllFragment(listType: ViewAllArg) {
        when (listType) {
            TOP_GAINERS -> binding.appNameTextView.text = getString(R.string.top_gainers)
            TOP_LOSERS -> binding.appNameTextView.text = getString(R.string.top_losers)
            MOST_TRADED -> binding.appNameTextView.text = getString(R.string.most_traded)
        }
    }
    private fun resumeExploreFragment() {
        binding.appNameTextView.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.VISIBLE
        binding.appNameTextView.visibility = View.VISIBLE

        binding.appNameTextView.text = getString(R.string.app_name)
        binding.editTextSearch.setText("")
        viewModel.clearSearch()
        binding.editTextSearch.clearFocus()
    }
    private fun resumeViewProductFragment() {
        binding.appNameTextView.visibility = View.GONE
        binding.editTextSearch.visibility = View.GONE
    }

}