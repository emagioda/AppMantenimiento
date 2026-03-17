package com.emagioda.myapp.presentation.viewmodel

import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.model.MaintenanceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryViewModelTest {

    @Test
    fun sortMaintenanceCases_prioritizesUnresolvedBeforeFinalized() {
        val sorted = sortMaintenanceCases(
            cases = listOf(
                case(id = 1L, status = MaintenanceStatus.FINALIZED, updatedAt = 500L),
                case(id = 2L, status = MaintenanceStatus.IN_PROGRESS, updatedAt = 200L),
                case(id = 3L, status = MaintenanceStatus.PENDING, updatedAt = 100L),
                case(id = 4L, status = MaintenanceStatus.PENDING, updatedAt = 400L),
                case(id = 5L, status = MaintenanceStatus.FINALIZED, updatedAt = 300L)
            ),
            prioritizeUnresolved = true
        )

        assertEquals(listOf(4L, 3L, 2L, 1L, 5L), sorted.map { it.id })
    }

    @Test
    fun sortMaintenanceCases_keepsIncomingOrderWhenPrioritizationIsDisabled() {
        val original = listOf(
            case(id = 9L, status = MaintenanceStatus.FINALIZED, updatedAt = 100L),
            case(id = 7L, status = MaintenanceStatus.PENDING, updatedAt = 900L),
            case(id = 8L, status = MaintenanceStatus.IN_PROGRESS, updatedAt = 300L)
        )

        val sorted = sortMaintenanceCases(
            cases = original,
            prioritizeUnresolved = false
        )

        assertEquals(original.map { it.id }, sorted.map { it.id })
    }

    private fun case(
        id: Long,
        status: MaintenanceStatus,
        updatedAt: Long
    ): MaintenanceCaseSummary =
        MaintenanceCaseSummary(
            id = id,
            machineId = "MACHINE_$id",
            machineNameSnapshot = "Macchina $id",
            problemSummary = "Problema $id",
            diagnosisTitle = "Diagnosi $id",
            diagnosisDescription = null,
            endResult = EndResult.COMPONENT_FAULT,
            status = status,
            openedAt = updatedAt - 10L,
            updatedAt = updatedAt,
            resolvedAt = if (status == MaintenanceStatus.FINALIZED) updatedAt else null,
            canceledAt = null,
            cancellationReason = null,
            latestEventTitle = null,
            latestEventAt = null
        )
}
