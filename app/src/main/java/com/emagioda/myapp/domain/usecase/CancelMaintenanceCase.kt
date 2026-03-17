package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.CancelMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class CancelMaintenanceCase(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: CancelMaintenanceCaseRequest) =
        repository.cancelCase(request)
}
