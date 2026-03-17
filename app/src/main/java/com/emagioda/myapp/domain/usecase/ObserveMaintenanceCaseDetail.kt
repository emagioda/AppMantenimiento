package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveMaintenanceCaseDetail(
    private val repository: MaintenanceHistoryRepository
) {
    operator fun invoke(caseId: Long): Flow<MaintenanceCaseDetail?> =
        repository.observeCaseDetail(caseId)
}
