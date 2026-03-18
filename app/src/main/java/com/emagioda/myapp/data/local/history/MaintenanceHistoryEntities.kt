package com.emagioda.myapp.data.local.history

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus

@Entity(tableName = "maintenance_cases")
data class MaintenanceCaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseCode: String,
    val machineId: String,
    val machineNameSnapshot: String,
    val endNodeId: String,
    val diagnosisTitle: String,
    val diagnosisDescription: String?,
    val endResult: EndResult,
    val status: MaintenanceStatus,
    val openedAt: Long,
    val updatedAt: Long,
    val resolvedAt: Long?,
    val canceledAt: Long?,
    val cancellationReason: String?
)

@Entity(tableName = "maintenance_events")
data class MaintenanceEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long,
    val type: MaintenanceEventType,
    val title: String,
    val note: String?,
    val createdAt: Long
)

data class MaintenanceCaseSummaryRow(
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

data class MaintenanceCaseWithEvents(
    @Embedded val maintenanceCase: MaintenanceCaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "caseId"
    )
    val events: List<MaintenanceEventEntity>
)
