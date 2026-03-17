package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.MachineDetail
import com.emagioda.myapp.domain.usecase.GetMachineDetail
import com.emagioda.myapp.domain.usecase.ObserveMaintenanceCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MachineHistoryOverviewUiState(
    val openCasesCount: Int = 0,
    val hasOpenCases: Boolean = false
)

data class MachineDetailUiState(
    val machine: MachineDetail? = null,
    val isLoading: Boolean = true,
    val errorResId: Int? = null,
    val historyOverview: MachineHistoryOverviewUiState = MachineHistoryOverviewUiState()
)

class MachineDetailViewModel(
    private val getMachineDetail: GetMachineDetail,
    private val observeMaintenanceCases: ObserveMaintenanceCases,
    private val machineId: String
) : ViewModel() {

    var uiState by mutableStateOf(MachineDetailUiState())
        private set

    init {
        load()
        observeHistory()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val machine = getMachineDetail(machineId)
                withContext(Dispatchers.Main) {
                    uiState = if (machine != null) {
                        uiState.copy(
                            machine = machine,
                            isLoading = false,
                            errorResId = null
                        )
                    } else {
                        uiState.copy(
                            isLoading = false,
                            errorResId = R.string.machine_detail_error_loading
                        )
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorResId = R.string.machine_detail_error_loading
                    )
                }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            observeMaintenanceCases(machineId).collectLatest { cases ->
                val openCasesCount = cases.count {
                    it.status == MaintenanceStatus.PENDING || it.status == MaintenanceStatus.IN_PROGRESS
                }
                uiState = uiState.copy(
                    historyOverview = MachineHistoryOverviewUiState(
                        openCasesCount = openCasesCount,
                        hasOpenCases = openCasesCount > 0
                    )
                )
            }
        }
    }

    class Factory(
        private val getMachineDetail: GetMachineDetail,
        private val observeMaintenanceCases: ObserveMaintenanceCases,
        private val machineId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MachineDetailViewModel(
                getMachineDetail = getMachineDetail,
                observeMaintenanceCases = observeMaintenanceCases,
                machineId = machineId
            ) as T
        }
    }
}
