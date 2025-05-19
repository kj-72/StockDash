package com.example.stockdash

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.stockdash.data.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class StockDashApp : Application() {

    @Inject // Hilt injects this repository
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // Create a custom scope for application-level coroutines that shouldn't be tied to UI
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        // Apply the theme preference at startup
        applicationScope.launch {
            val currentThemeMode = userPreferencesRepository.appThemeMode.first() // Get the first/current value
            AppCompatDelegate.setDefaultNightMode(currentThemeMode)
        }
    }
}