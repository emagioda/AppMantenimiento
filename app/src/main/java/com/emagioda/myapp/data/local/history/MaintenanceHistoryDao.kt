package com.emagioda.myapp.data.local.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceHistoryDao {

    @Query(
        """
        SELECT
            c.id,
            c.machineId,
            c.machineNameSnapshot,
            COALESCE(
                (
                    SELECT NULLIF(TRIM(e.note), '')
                    FROM maintenance_events e
                    WHERE e.caseId = c.id
                      AND e.type = 'PROBLEM_DETECTED'
                    ORDER BY e.createdAt ASC, e.id ASC
                    LIMIT 1
                ),
                c.diagnosisTitle
            ) AS problemSummary,
            c.diagnosisTitle,
            c.diagnosisDescription,
            c.endResult,
            c.status,
            c.openedAt,
            c.updatedAt,
            c.resolvedAt,
            c.canceledAt,
            c.cancellationReason,
            (
                SELECT e.title
                FROM maintenance_events e
                WHERE e.caseId = c.id
                ORDER BY e.createdAt DESC, e.id DESC
                LIMIT 1
            ) AS latestEventTitle,
            (
                SELECT e.createdAt
                FROM maintenance_events e
                WHERE e.caseId = c.id
                ORDER BY e.createdAt DESC, e.id DESC
                LIMIT 1
            ) AS latestEventAt
        FROM maintenance_cases c
        WHERE (:machineId IS NULL OR c.machineId = :machineId)
          AND (:includeCanceled = 1 OR c.status != 'CANCELED')
        ORDER BY c.updatedAt DESC, c.id DESC
        """
    )
    fun observeCaseSummaries(machineId: String?, includeCanceled: Boolean): Flow<List<MaintenanceCaseSummaryRow>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM maintenance_cases WHERE id = :caseId LIMIT 1")
    fun observeCaseWithEvents(caseId: Long): Flow<MaintenanceCaseWithEvents?>

    @Insert
    suspend fun insertCase(entity: MaintenanceCaseEntity): Long

    @Insert
    suspend fun insertEvent(entity: MaintenanceEventEntity): Long

    @Update
    suspend fun updateCase(entity: MaintenanceCaseEntity)

    @Update
    suspend fun updateEvent(entity: MaintenanceEventEntity)

    @Query("SELECT * FROM maintenance_cases WHERE id = :caseId LIMIT 1")
    suspend fun getCaseById(caseId: Long): MaintenanceCaseEntity?

    @Query(
        """
        SELECT * FROM maintenance_events
        WHERE caseId = :caseId AND type = 'PROBLEM_DETECTED'
        ORDER BY createdAt ASC, id ASC
        LIMIT 1
        """
    )
    suspend fun getFirstProblemEvent(caseId: Long): MaintenanceEventEntity?
}
