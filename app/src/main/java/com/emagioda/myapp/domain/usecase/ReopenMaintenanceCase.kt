package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.ReopenMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class ReopenMaintenanceCase(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: ReopenMaintenanceCaseRequest) =
        repository.reopenCase(request)
}
