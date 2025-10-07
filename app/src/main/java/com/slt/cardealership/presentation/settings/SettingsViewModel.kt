package com.slt.cardealership.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slt.cardealership.data.local.ThemeDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeDataStore: ThemeDataStore
) : ViewModel() {

    val isDarkMode = themeDataStore.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun onThemeChange(isDark: Boolean) {
        viewModelScope.launch {
            themeDataStore.setDarkMode(isDark)
        }
    }
}