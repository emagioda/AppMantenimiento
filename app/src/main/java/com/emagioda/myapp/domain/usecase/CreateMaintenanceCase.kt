package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.CreateMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class CreateMaintenanceCase(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: CreateMaintenanceCaseRequest): Long =
        repository.createCase(request)
}
