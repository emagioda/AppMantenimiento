package com.emagioda.myapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.domain.usecase.GetMachineIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class ScannerUiState(
    val machineIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ScannerViewModel(
    private val getMachineIds: GetMachineIds
) : ViewModel() {

    var uiState by mutableStateOf(ScannerUiState())
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ids = getMachineIds()
                withContext(Dispatchers.Main) {
                    uiState = ScannerUiState(machineIds = ids, isLoading = false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = ScannerUiState(
                        machineIds = emptySet(),
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    class Factory(
        private val getMachineIds: GetMachineIds
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScannerViewModel(getMachineIds) as T
        }
    }
}
