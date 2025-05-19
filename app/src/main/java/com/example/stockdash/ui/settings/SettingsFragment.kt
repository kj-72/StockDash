package com.example.stockdash.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.stockdash.databinding.FragmentSettingsBinding
import com.example.stockdash.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDarkMode.setOnClickListener {
            showThemeChooserDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setUiStateForSettings()
    }



    private fun showThemeChooserDialog() {
        val themes = arrayOf("Light", "Dark", "Follow System")
        val modes = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        val currentMode = settingsViewModel.currentThemeMode.value // Get current mode for pre-selection
        var checkedItem = modes.indexOf(currentMode)
        if (checkedItem == -1) checkedItem = 2 // Default to "Follow System" index

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedMode = modes[which]
                settingsViewModel.changeThemeMode(selectedMode) // Tell ViewModel to save and apply

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}