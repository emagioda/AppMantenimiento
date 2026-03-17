package com.emagioda.myapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.emagioda.myapp.data.local.history.MaintenanceHistoryDatabase
import com.emagioda.myapp.data.repository.MaintenanceHistoryRepositoryImpl
import com.emagioda.myapp.domain.model.CreateMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.CreateMaintenanceEventRequest
import com.emagioda.myapp.domain.model.CancelMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.InitialMaintenanceAction
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.ReopenMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.ResolveMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.UpdateMaintenanceCaseRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MaintenanceHistoryRepositoryTest {

    private lateinit var database: MaintenanceHistoryDatabase
    private lateinit var repository: MaintenanceHistoryRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MaintenanceHistoryDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        repository = MaintenanceHistoryRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createCase_registersInitialProblemAndAction() = runBlocking {
        val caseId = repository.createCase(
            baseRequest(
                initialAction = InitialMaintenanceAction.TECHNICIAN_CONTACTED,
                initialActionTitle = "Tecnico contattato"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertNotNull(detail)
        assertEquals(MaintenanceStatus.PENDING, detail?.status)
        assertEquals(2, detail?.events?.size)
        assertEquals(MaintenanceEventType.PROBLEM_DETECTED, detail?.events?.first()?.type)
        assertEquals(MaintenanceEventType.TECHNICIAN_CONTACTED, detail?.events?.last()?.type)
    }

    @Test
    fun addEvent_movesCaseToInProgress() = runBlocking {
        val caseId = repository.createCase(baseRequest())

        repository.addEvent(
            CreateMaintenanceEventRequest(
                caseId = caseId,
                type = MaintenanceEventType.TEST_PERFORMED,
                title = "Regolazione o prova eseguita",
                note = "Sistema ricalibrato"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertEquals(MaintenanceStatus.IN_PROGRESS, detail?.status)
        assertEquals(2, detail?.events?.size)
        assertEquals(MaintenanceEventType.TEST_PERFORMED, detail?.events?.last()?.type)
    }

    @Test
    fun resolveCase_marksCaseFinalizedAndStoresResolvedAt() = runBlocking {
        val caseId = repository.createCase(baseRequest())

        repository.resolveCase(
            ResolveMaintenanceCaseRequest(
                caseId = caseId,
                resolutionTitle = "Intervento chiuso",
                resolutionNote = "Componente sostituito"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertEquals(MaintenanceStatus.FINALIZED, detail?.status)
        assertNotNull(detail?.resolvedAt)
        assertEquals(MaintenanceEventType.RESOLUTION, detail?.events?.last()?.type)
    }

    @Test
    fun observeCases_filtersByMachineId() = runBlocking {
        repository.createCase(baseRequest(machineId = "MACHINE_A", machineName = "Macchina A"))
        repository.createCase(baseRequest(machineId = "MACHINE_B", machineName = "Macchina B"))

        val filtered = repository.observeCases("MACHINE_A").first()

        assertEquals(1, filtered.size)
        assertEquals("MACHINE_A", filtered.first().machineId)
    }

    @Test
    fun createFinalizedCase_generatesResolvedTimestamp() = runBlocking {
        val caseId = repository.createCase(
            baseRequest(
                status = MaintenanceStatus.FINALIZED,
                endResult = EndResult.RESOLVED,
                autoResolutionTitle = "Intervento chiuso"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertEquals(MaintenanceStatus.FINALIZED, detail?.status)
        assertTrue((detail?.events?.size ?: 0) >= 2)
        assertNotNull(detail?.resolvedAt)
    }

    @Test
    fun updateCase_changesProblemSummaryAndAddsAuditEvent() = runBlocking {
        val caseId = repository.createCase(baseRequest())

        repository.updateCase(
            UpdateMaintenanceCaseRequest(
                caseId = caseId,
                problemSummary = "La coclea si blocca in avvio",
                technicalSummary = "Verificare il sensore di finecorsa",
                updateTitle = "Intervento aggiornato",
                updateNote = "Descrizione iniziale corretta"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertEquals("La coclea si blocca in avvio", detail?.problemSummary)
        assertEquals("La coclea si blocca in avvio", detail?.diagnosisTitle)
        assertEquals("Verificare il sensore di finecorsa", detail?.diagnosisDescription)
        assertEquals(MaintenanceEventType.CASE_UPDATED, detail?.events?.last()?.type)
    }

    @Test
    fun reopenCase_movesFinalizedCaseBackToInProgress() = runBlocking {
        val caseId = repository.createCase(
            baseRequest(
                status = MaintenanceStatus.FINALIZED,
                endResult = EndResult.RESOLVED,
                autoResolutionTitle = "Intervento chiuso"
            )
        )

        repository.reopenCase(
            ReopenMaintenanceCaseRequest(
                caseId = caseId,
                reopenTitle = "Intervento riaperto",
                reopenNote = "Il problema si e ripresentato"
            )
        )

        val detail = repository.observeCaseDetail(caseId).first()

        assertEquals(MaintenanceStatus.IN_PROGRESS, detail?.status)
        assertEquals(null, detail?.resolvedAt)
        assertEquals(MaintenanceEventType.CASE_REOPENED, detail?.events?.last()?.type)
    }

    @Test
    fun cancelCase_hidesCaseFromSummariesButKeepsDetail() = runBlocking {
        val caseId = repository.createCase(baseRequest())

        repository.cancelCase(
            CancelMaintenanceCaseRequest(
                caseId = caseId,
                cancelTitle = "Intervento annullato",
                reason = "Registrato per errore"
            )
        )

        val cases = repository.observeCases().first()
        val detail = repository.observeCaseDetail(caseId).first()

        assertTrue(cases.none { it.id == caseId })
        assertEquals(MaintenanceStatus.CANCELED, detail?.status)
        assertNotNull(detail?.canceledAt)
        assertEquals("Registrato per errore", detail?.cancellationReason)
    }

    private fun baseRequest(
        machineId: String = "TRIMEC_SILO_001",
        machineName: String = "Silo de prueba",
        status: MaintenanceStatus = MaintenanceStatus.PENDING,
        endResult: EndResult = EndResult.COMPONENT_FAULT,
        initialAction: InitialMaintenanceAction = InitialMaintenanceAction.NONE,
        initialActionTitle: String? = null,
        autoResolutionTitle: String? = null
    ): CreateMaintenanceCaseRequest =
        CreateMaintenanceCaseRequest(
            machineId = machineId,
            machineNameSnapshot = machineName,
            endNodeId = "END_NODE",
            diagnosisTitle = "Motore bloccato",
            diagnosisDescription = "Si rileva un guasto al motore",
            endResult = endResult,
            status = status,
            problemTitle = "Problema rilevato",
            problemNote = "La macchina si e fermata",
            initialAction = initialAction,
            initialActionTitle = initialActionTitle,
            initialActionNote = null,
            autoResolutionTitle = autoResolutionTitle,
            autoResolutionNote = null
        )
}
