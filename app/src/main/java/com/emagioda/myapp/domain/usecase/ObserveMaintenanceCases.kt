package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveMaintenanceCases(
    private val repository: MaintenanceHistoryRepository
) {
    operator fun invoke(
        machineId: String? = null,
        includeCanceled: Boolean = false
    ): Flow<List<MaintenanceCaseSummary>> =
        repository.observeCases(machineId, includeCanceled)
}
