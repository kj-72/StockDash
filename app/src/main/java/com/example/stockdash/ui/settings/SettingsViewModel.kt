package com.example.stockdash.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdash.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val currentThemeMode: StateFlow<Int> = userPreferencesRepository.appThemeMode
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun changeThemeMode(mode: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setAppThemeMode(mode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}