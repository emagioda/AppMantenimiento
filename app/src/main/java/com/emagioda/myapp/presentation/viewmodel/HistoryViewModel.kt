package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.usecase.GetMachineDetail
import com.emagioda.myapp.domain.usecase.ObserveMaintenanceCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HistoryUiState(
    val machineIdFilter: String? = null,
    val machineNameFilter: String? = null,
    val cases: List<MaintenanceCaseSummary> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: MaintenanceStatus? = null,
    val showCanceledOnly: Boolean = false,
    val isLoading: Boolean = true,
    val openCasesCount: Int = 0,
    val hasOpenCases: Boolean = false,
    val prioritizeUnresolved: Boolean = false
)

class HistoryViewModel(
    private val machineIdFilter: String?,
    private val getMachineDetail: GetMachineDetail,
    private val observeMaintenanceCases: ObserveMaintenanceCases,
    private val prioritizeUnresolved: Boolean = false
) : ViewModel() {

    private var allCases: List<MaintenanceCaseSummary> = emptyList()

    var uiState by mutableStateOf(
        HistoryUiState(
            machineIdFilter = machineIdFilter,
            prioritizeUnresolved = prioritizeUnresolved
        )
    )
        private set

    init {
        loadMachineName()
        observeCases()
    }

    fun onSearchQueryChange(value: String) {
        uiState = uiState.copy(searchQuery = value)
        publishFilteredCases()
    }

    fun onStatusToggle(status: MaintenanceStatus) {
        uiState = uiState.copy(
            selectedStatus = if (uiState.selectedStatus == status) null else status,
            showCanceledOnly = false
        )
        publishFilteredCases()
    }

    fun onCanceledToggle() {
        val nextShowCanceledOnly = !uiState.showCanceledOnly
        uiState = uiState.copy(
            showCanceledOnly = nextShowCanceledOnly,
            selectedStatus = if (nextShowCanceledOnly) null else uiState.selectedStatus
        )
        publishFilteredCases()
    }

    private fun loadMachineName() {
        if (machineIdFilter.isNullOrBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val machineName = getMachineDetail(machineIdFilter)?.name ?: machineIdFilter
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(machineNameFilter = machineName)
            }
        }
    }

    private fun observeCases() {
        viewModelScope.launch {
            observeMaintenanceCases(
                machineId = machineIdFilter,
                includeCanceled = true
            ).collectLatest { cases ->
                allCases = cases
                val openCasesCount = cases.count {
                    it.status == MaintenanceStatus.PENDING || it.status == MaintenanceStatus.IN_PROGRESS
                }
                uiState = uiState.copy(
                    isLoading = false,
                    openCasesCount = openCasesCount,
                    hasOpenCases = openCasesCount > 0
                )
                publishFilteredCases()
            }
        }
    }

    private fun publishFilteredCases() {
        val query = uiState.searchQuery.trim()
        val filtered = allCases.filter { item ->
            val matchesStatus = when (uiState.selectedStatus) {
                null -> if (uiState.showCanceledOnly) {
                    item.status == MaintenanceStatus.CANCELED
                } else {
                    item.status != MaintenanceStatus.CANCELED
                }
                else -> item.status == uiState.selectedStatus
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val matchesMachineIdentity = machineIdFilter.isNullOrBlank() && (
                    item.machineNameSnapshot.contains(query, ignoreCase = true) ||
                        item.machineId.contains(query, ignoreCase = true)
                    )
                matchesMachineIdentity ||
                    item.diagnosisTitle.contains(query, ignoreCase = true) ||
                    item.problemSummary?.contains(query, ignoreCase = true) == true ||
                    item.latestEventTitle?.contains(query, ignoreCase = true) == true
            }

            matchesStatus && matchesQuery
        }

        uiState = uiState.copy(
            cases = sortMaintenanceCases(
                cases = filtered,
                prioritizeUnresolved = uiState.prioritizeUnresolved
            )
        )
    }

    class Factory(
        private val machineIdFilter: String?,
        private val getMachineDetail: GetMachineDetail,
        private val observeMaintenanceCases: ObserveMaintenanceCases,
        private val prioritizeUnresolved: Boolean = false
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(
                machineIdFilter = machineIdFilter,
                getMachineDetail = getMachineDetail,
                observeMaintenanceCases = observeMaintenanceCases,
                prioritizeUnresolved = prioritizeUnresolved
            ) as T
        }
    }
}

internal fun sortMaintenanceCases(
    cases: List<MaintenanceCaseSummary>,
    prioritizeUnresolved: Boolean
): List<MaintenanceCaseSummary> {
    if (!prioritizeUnresolved) return cases

    return cases.sortedWith(
        compareBy<MaintenanceCaseSummary> { case ->
            when (case.status) {
                MaintenanceStatus.PENDING -> 0
                MaintenanceStatus.IN_PROGRESS -> 1
                MaintenanceStatus.FINALIZED -> 2
                MaintenanceStatus.CANCELED -> 3
            }
        }
            .thenByDescending { case -> case.updatedAt }
            .thenByDescending { case -> case.id }
    )
}
