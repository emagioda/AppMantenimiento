package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.UpdateMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class UpdateMaintenanceCase(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: UpdateMaintenanceCaseRequest) =
        repository.updateCase(request)
}
