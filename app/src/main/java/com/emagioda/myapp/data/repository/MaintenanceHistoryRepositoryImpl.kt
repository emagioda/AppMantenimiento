package com.emagioda.myapp.data.repository

import androidx.room.withTransaction
import com.emagioda.myapp.data.local.history.MaintenanceCaseEntity
import com.emagioda.myapp.data.local.history.MaintenanceCaseSummaryRow
import com.emagioda.myapp.data.local.history.MaintenanceEventEntity
import com.emagioda.myapp.data.local.history.MaintenanceHistoryDao
import com.emagioda.myapp.data.local.history.MaintenanceHistoryDatabase
import com.emagioda.myapp.domain.model.CancelMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.CreateMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.CreateMaintenanceEventRequest
import com.emagioda.myapp.domain.model.InitialMaintenanceAction
import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.MaintenanceTimelineEvent
import com.emagioda.myapp.domain.model.ReopenMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.ResolveMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.UpdateMaintenanceCaseRequest
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaintenanceHistoryRepositoryImpl(
    private val database: MaintenanceHistoryDatabase,
    private val dao: MaintenanceHistoryDao = database.maintenanceHistoryDao()
) : MaintenanceHistoryRepository {

    override fun observeCases(
        machineId: String?,
        includeCanceled: Boolean
    ): Flow<List<MaintenanceCaseSummary>> =
        dao.observeCaseSummaries(machineId, includeCanceled).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeCaseDetail(caseId: Long): Flow<MaintenanceCaseDetail?> =
        dao.observeCaseWithEvents(caseId).map { row ->
            row?.let {
                val sortedEvents = it.events
                    .sortedWith(compareBy<MaintenanceEventEntity> { event -> event.createdAt }
                        .thenBy { event -> event.id })
                val problemSummary = sortedEvents
                    .firstOrNull { event -> event.type == MaintenanceEventType.PROBLEM_DETECTED }
                    ?.note
                    ?.takeIf { note -> note.isNotBlank() }
                    ?: it.maintenanceCase.diagnosisTitle

                MaintenanceCaseDetail(
                    id = it.maintenanceCase.id,
                    caseCode = it.maintenanceCase.caseCode,
                    machineId = it.maintenanceCase.machineId,
                    machineNameSnapshot = it.maintenanceCase.machineNameSnapshot,
                    endNodeId = it.maintenanceCase.endNodeId,
                    problemSummary = problemSummary,
                    diagnosisTitle = it.maintenanceCase.diagnosisTitle,
                    diagnosisDescription = it.maintenanceCase.diagnosisDescription,
                    endResult = it.maintenanceCase.endResult,
                    status = it.maintenanceCase.status,
                    openedAt = it.maintenanceCase.openedAt,
                    updatedAt = it.maintenanceCase.updatedAt,
                    resolvedAt = it.maintenanceCase.resolvedAt,
                    canceledAt = it.maintenanceCase.canceledAt,
                    cancellationReason = it.maintenanceCase.cancellationReason,
                    events = sortedEvents
                        .map { event ->
                            MaintenanceTimelineEvent(
                                id = event.id,
                                caseId = event.caseId,
                                type = event.type,
                                title = event.title,
                                note = event.note,
                                createdAt = event.createdAt
                            )
                        }
                )
            }
        }

    override suspend fun createCase(request: CreateMaintenanceCaseRequest): Long =
        database.withTransaction {
            val now = System.currentTimeMillis()
            val resolvedAt = if (request.status == MaintenanceStatus.FINALIZED) now else null
            val caseCode = generateCaseCode(request.machineId, now)
            val caseId = dao.insertCase(
                MaintenanceCaseEntity(
                    caseCode = caseCode,
                    machineId = request.machineId,
                    machineNameSnapshot = request.machineNameSnapshot,
                    endNodeId = request.endNodeId,
                    diagnosisTitle = request.diagnosisTitle,
                    diagnosisDescription = request.diagnosisDescription,
                    endResult = request.endResult,
                    status = request.status,
                    openedAt = now,
                    updatedAt = now,
                    resolvedAt = resolvedAt,
                    canceledAt = null,
                    cancellationReason = null
                )
            )

            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = caseId,
                    type = MaintenanceEventType.PROBLEM_DETECTED,
                    title = request.problemTitle,
                    note = request.problemNote?.takeIf { it.isNotBlank() },
                    createdAt = now
                )
            )

            val actionType = request.initialAction.toEventType()
            if (actionType != null) {
                dao.insertEvent(
                    MaintenanceEventEntity(
                        caseId = caseId,
                        type = actionType,
                        title = request.initialActionTitle.orEmpty().ifBlank { actionType.name },
                        note = request.initialActionNote?.takeIf { it.isNotBlank() },
                        createdAt = now
                    )
                )
            }

            if (request.status == MaintenanceStatus.FINALIZED) {
                dao.insertEvent(
                    MaintenanceEventEntity(
                        caseId = caseId,
                        type = MaintenanceEventType.RESOLUTION,
                        title = request.autoResolutionTitle.orEmpty()
                            .ifBlank { MaintenanceEventType.RESOLUTION.name },
                        note = request.autoResolutionNote?.takeIf { it.isNotBlank() },
                        createdAt = now
                    )
                )
            }

            caseId
        }

    override suspend fun addEvent(request: CreateMaintenanceEventRequest) {
        database.withTransaction {
            val existing = dao.getCaseById(request.caseId) ?: return@withTransaction
            if (existing.status == MaintenanceStatus.CANCELED) return@withTransaction
            val now = System.currentTimeMillis()
            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = request.caseId,
                    type = request.type,
                    title = request.title,
                    note = request.note?.takeIf { it.isNotBlank() },
                    createdAt = now
                )
            )
            if (existing.status != MaintenanceStatus.FINALIZED) {
                dao.updateCase(
                    existing.copy(
                        status = MaintenanceStatus.IN_PROGRESS,
                        updatedAt = now,
                        canceledAt = null,
                        cancellationReason = null
                    )
                )
            }
        }
    }

    override suspend fun resolveCase(request: ResolveMaintenanceCaseRequest) {
        database.withTransaction {
            val existing = dao.getCaseById(request.caseId) ?: return@withTransaction
            if (existing.status == MaintenanceStatus.CANCELED) return@withTransaction
            val now = System.currentTimeMillis()
            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = request.caseId,
                    type = MaintenanceEventType.RESOLUTION,
                    title = request.resolutionTitle,
                    note = request.resolutionNote?.takeIf { it.isNotBlank() },
                    createdAt = now
                )
            )
            dao.updateCase(
                existing.copy(
                    status = MaintenanceStatus.FINALIZED,
                    updatedAt = now,
                    resolvedAt = now,
                    canceledAt = null,
                    cancellationReason = null
                )
            )
        }
    }

    override suspend fun updateCase(request: UpdateMaintenanceCaseRequest) {
        database.withTransaction {
            val existing = dao.getCaseById(request.caseId) ?: return@withTransaction
            if (existing.status == MaintenanceStatus.CANCELED) return@withTransaction

            val now = System.currentTimeMillis()
            val trimmedProblem = request.problemSummary.trim()
            val technicalSummary = request.technicalSummary?.trim()?.takeIf { it.isNotBlank() }
            val problemEvent = dao.getFirstProblemEvent(request.caseId)

            if (problemEvent != null) {
                dao.updateEvent(
                    problemEvent.copy(
                        note = trimmedProblem
                    )
                )
            }

            dao.updateCase(
                existing.copy(
                    diagnosisTitle = trimmedProblem,
                    diagnosisDescription = technicalSummary,
                    updatedAt = now
                )
            )

            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = request.caseId,
                    type = MaintenanceEventType.CASE_UPDATED,
                    title = request.updateTitle,
                    note = request.updateNote?.trim()?.takeIf { it.isNotBlank() } ?: trimmedProblem,
                    createdAt = now
                )
            )
        }
    }

    override suspend fun reopenCase(request: ReopenMaintenanceCaseRequest) {
        database.withTransaction {
            val existing = dao.getCaseById(request.caseId) ?: return@withTransaction
            if (existing.status != MaintenanceStatus.FINALIZED) return@withTransaction

            val now = System.currentTimeMillis()
            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = request.caseId,
                    type = MaintenanceEventType.CASE_REOPENED,
                    title = request.reopenTitle,
                    note = request.reopenNote?.trim()?.takeIf { it.isNotBlank() },
                    createdAt = now
                )
            )
            dao.updateCase(
                existing.copy(
                    status = MaintenanceStatus.IN_PROGRESS,
                    updatedAt = now,
                    resolvedAt = null,
                    canceledAt = null,
                    cancellationReason = null
                )
            )
        }
    }

    override suspend fun cancelCase(request: CancelMaintenanceCaseRequest) {
        database.withTransaction {
            val existing = dao.getCaseById(request.caseId) ?: return@withTransaction
            if (existing.status == MaintenanceStatus.CANCELED) return@withTransaction

            val now = System.currentTimeMillis()
            val reason = request.reason.trim()
            dao.insertEvent(
                MaintenanceEventEntity(
                    caseId = request.caseId,
                    type = MaintenanceEventType.CASE_CANCELED,
                    title = request.cancelTitle,
                    note = reason,
                    createdAt = now
                )
            )
            dao.updateCase(
                existing.copy(
                    status = MaintenanceStatus.CANCELED,
                    updatedAt = now,
                    resolvedAt = null,
                    canceledAt = now,
                    cancellationReason = reason
                )
            )
        }
    }

    private fun MaintenanceCaseSummaryRow.toDomain(): MaintenanceCaseSummary =
        MaintenanceCaseSummary(
            id = id,
            caseCode = caseCode,
            machineId = machineId,
            machineNameSnapshot = machineNameSnapshot,
            problemSummary = problemSummary,
            diagnosisTitle = diagnosisTitle,
            diagnosisDescription = diagnosisDescription,
            endResult = endResult,
            status = status,
            openedAt = openedAt,
            updatedAt = updatedAt,
            resolvedAt = resolvedAt,
            canceledAt = canceledAt,
            cancellationReason = cancellationReason,
            latestEventTitle = latestEventTitle,
            latestEventAt = latestEventAt
        )

    private fun InitialMaintenanceAction.toEventType(): MaintenanceEventType? =
        when (this) {
            InitialMaintenanceAction.NONE -> null
            InitialMaintenanceAction.TECHNICIAN_CONTACTED -> MaintenanceEventType.TECHNICIAN_CONTACTED
            InitialMaintenanceAction.COMPONENT_REPLACED -> MaintenanceEventType.COMPONENT_REPLACED
            InitialMaintenanceAction.TEST_PERFORMED -> MaintenanceEventType.TEST_PERFORMED
            InitialMaintenanceAction.OTHER -> MaintenanceEventType.OTHER
        }

    private fun generateCaseCode(machineId: String, createdAt: Long): String {
        val machineCode = machineId
            .trim()
            .uppercase(Locale.ROOT)
            .replace("\\s+".toRegex(), "_")
            .replace("[^A-Z0-9_-]".toRegex(), "")
        val timestamp = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
            .format(Date(createdAt))
        return "$machineCode-$timestamp"
    }
}
