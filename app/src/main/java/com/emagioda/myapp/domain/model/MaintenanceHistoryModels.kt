package com.emagioda.myapp.domain.model

data class MaintenanceCaseSummary(
    val id: Long,
    val caseCode: String,
    val machineId: String,
    val machineNameSnapshot: String,
    val problemSummary: String?,
    val diagnosisTitle: String,
    val diagnosisDescription: String?,
    val endResult: EndResult,
    val status: MaintenanceStatus,
    val openedAt: Long,
    val updatedAt: Long,
    val resolvedAt: Long?,
    val canceledAt: Long?,
    val cancellationReason: String?,
    val latestEventTitle: String?,
    val latestEventAt: Long?
)

data class MaintenanceCaseDetail(
    val id: Long,
    val caseCode: String,
    val machineId: String,
    val machineNameSnapshot: String,
    val endNodeId: String,
    val problemSummary: String?,
    val diagnosisTitle: String,
    val diagnosisDescription: String?,
    val endResult: EndResult,
    val status: MaintenanceStatus,
    val openedAt: Long,
    val updatedAt: Long,
    val resolvedAt: Long?,
    val canceledAt: Long?,
    val cancellationReason: String?,
    val events: List<MaintenanceTimelineEvent>
)

data class MaintenanceTimelineEvent(
    val id: Long,
    val caseId: Long,
    val type: MaintenanceEventType,
    val title: String,
    val note: String?,
    val createdAt: Long
)

data class CreateMaintenanceCaseRequest(
    val machineId: String,
    val machineNameSnapshot: String,
    val endNodeId: String,
    val diagnosisTitle: String,
    val diagnosisDescription: String?,
    val endResult: EndResult,
    val status: MaintenanceStatus,
    val problemTitle: String,
    val problemNote: String?,
    val initialAction: InitialMaintenanceAction = InitialMaintenanceAction.NONE,
    val initialActionTitle: String? = null,
    val initialActionNote: String? = null,
    val autoResolutionTitle: String? = null,
    val autoResolutionNote: String? = null
)

data class CreateMaintenanceEventRequest(
    val caseId: Long,
    val type: MaintenanceEventType,
    val title: String,
    val note: String? = null
)

data class ResolveMaintenanceCaseRequest(
    val caseId: Long,
    val resolutionTitle: String,
    val resolutionNote: String? = null
)

data class UpdateMaintenanceCaseRequest(
    val caseId: Long,
    val problemSummary: String,
    val technicalSummary: String? = null,
    val updateTitle: String,
    val updateNote: String? = null
)

data class ReopenMaintenanceCaseRequest(
    val caseId: Long,
    val reopenTitle: String,
    val reopenNote: String? = null
)

data class CancelMaintenanceCaseRequest(
    val caseId: Long,
    val cancelTitle: String,
    val reason: String
)

enum class MaintenanceStatus {
    PENDING,
    IN_PROGRESS,
    FINALIZED,
    CANCELED
}

enum class MaintenanceEventType {
    PROBLEM_DETECTED,
    TECHNICIAN_CONTACTED,
    COMPONENT_REPLACED,
    TEST_PERFORMED,
    OBSERVATION,
    OTHER,
    RESOLUTION,
    CASE_UPDATED,
    CASE_REOPENED,
    CASE_CANCELED
}

enum class InitialMaintenanceAction {
    NONE,
    TECHNICIAN_CONTACTED,
    COMPONENT_REPLACED,
    TEST_PERFORMED,
    OTHER
}
