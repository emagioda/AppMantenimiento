package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.CreateMaintenanceEventRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository

class AddMaintenanceEvent(
    private val repository: MaintenanceHistoryRepository
) {
    suspend operator fun invoke(request: CreateMaintenanceEventRequest) =
        repository.addEvent(request)
}
