package com.emagioda.myapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ThemeViewModel(private val appContext: Context) : ViewModel() {
    private val _isDark = MutableStateFlow<Boolean?>(null)
    val isDark: StateFlow<Boolean?> = _isDark

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch {
            ThemePreferences.observeDarkTheme(appContext).collectLatest { value ->
                val wasNull = _isDark.value == null
                _isDark.value = value
                if (wasNull) _isReady.value = true
            }
        }
    }

    fun setDark(enabled: Boolean) {
        viewModelScope.launch {
            ThemePreferences.setDarkTheme(appContext, enabled)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThemeViewModel(appContext.applicationContext) as T
        }
    }
}
