package com.emagioda.myapp.domain.repository

import com.emagioda.myapp.domain.model.CreateMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.CreateMaintenanceEventRequest
import com.emagioda.myapp.domain.model.CancelMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.model.ReopenMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.ResolveMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.UpdateMaintenanceCaseRequest
import kotlinx.coroutines.flow.Flow

interface MaintenanceHistoryRepository {
    fun observeCases(
        machineId: String? = null,
        includeCanceled: Boolean = false
    ): Flow<List<MaintenanceCaseSummary>>
    fun observeCaseDetail(caseId: Long): Flow<MaintenanceCaseDetail?>
    suspend fun createCase(request: CreateMaintenanceCaseRequest): Long
    suspend fun addEvent(request: CreateMaintenanceEventRequest)
    suspend fun resolveCase(request: ResolveMaintenanceCaseRequest)
    suspend fun updateCase(request: UpdateMaintenanceCaseRequest)
    suspend fun reopenCase(request: ReopenMaintenanceCaseRequest)
    suspend fun cancelCase(request: CancelMaintenanceCaseRequest)
}
