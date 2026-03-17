package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.domain.usecase.GetMachineDetail
import com.emagioda.myapp.domain.usecase.GetMachineIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HistoryMachineListItemUiState(
    val id: String,
    val name: String,
    val imageName: String?
)

data class HistoryMachinesUiState(
    val machines: List<HistoryMachineListItemUiState> = emptyList(),
    val isLoading: Boolean = true
)

class HistoryMachinesViewModel(
    private val getMachineIds: GetMachineIds,
    private val getMachineDetail: GetMachineDetail
) : ViewModel() {

    var uiState by mutableStateOf(HistoryMachinesUiState())
        private set

    init {
        loadMachines()
    }

    private fun loadMachines() {
        viewModelScope.launch(Dispatchers.IO) {
            val machines = getMachineIds()
                .mapNotNull { machineId ->
                    getMachineDetail(machineId)?.let { machine ->
                        HistoryMachineListItemUiState(
                            id = machine.id,
                            name = machine.name.ifBlank { machine.id },
                            imageName = machine.imageName
                        )
                    }
                }
                .sortedBy { machine -> machine.name.lowercase() }

            withContext(Dispatchers.Main) {
                uiState = HistoryMachinesUiState(
                    machines = machines,
                    isLoading = false
                )
            }
        }
    }

    class Factory(
        private val getMachineIds: GetMachineIds,
        private val getMachineDetail: GetMachineDetail
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryMachinesViewModel(
                getMachineIds = getMachineIds,
                getMachineDetail = getMachineDetail
            ) as T
        }
    }
}
