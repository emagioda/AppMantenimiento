package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.ResolveMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class ResolveMaintenanceCase(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: ResolveMaintenanceCaseRequest) =
        repository.resolveCase(request)
}
