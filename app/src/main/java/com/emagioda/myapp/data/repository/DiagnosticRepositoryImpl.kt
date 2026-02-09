package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class DiagnosticRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : DiagnosticRepository {

    override fun getTreeForMachine(machineId: String): DiagnosticTree {
        // 1) Trova la macchina -> template
        val machinesIndex = ds.readMachinesIndex()
        val machine = machinesIndex.machines.firstOrNull { it.id == machineId }
            ?: error("Machine not found for id=$machineId")

        // 2) Carica l'albero grezzo + catalogo ricambi
        val rawTree = ds.readTemplateRaw(machine.templateId)
        val partsCatalog = ds.readPartsCatalog()

        // 3) Creare una mappa veloce per cercare i dettagli delle parti per ID
        // Ora il JSON parts.json non ha nodeRefs, è solo un elenco.
        val partsMap = partsCatalog.parts.associateBy { it.id }

        // 4) Mappa i nodi grezzi -> nodi di dominio
        val nodes = rawTree.nodes.map { raw ->
            val type = when (raw.type.uppercase()) {
                "QUESTION" -> NodeType.QUESTION
                "END" -> NodeType.END
                else -> NodeType.QUESTION
            }

            val result = raw.result?.let { mapResult(it) }

            val mode = when (raw.mode?.uppercase()) {
                "CONTINUE_ONLY" -> QuestionMode.CONTINUE_ONLY
                "YES_NO", null -> QuestionMode.YES_NO
                else -> QuestionMode.YES_NO
            }

            // Nuova logica: Le parti sono definite nel nodo (raw.parts).
            // Dobbiamo prendere l'ID dal nodo e cercare i dettagli completi nel partsMap.
            val nodeParts = raw.parts.orEmpty().mapNotNull { ref ->
                val detailRaw = partsMap[ref.id] ?: return@mapNotNull null

                PartRefResolved(
                    detail = detailRaw.toDomain(),
                    qty = ref.qty
                )
            }

            DiagnosticNode(
                id = raw.id,
                type = type,
                title = raw.title,
                description = raw.description,
                yes = raw.yes,
                no = raw.no,
                providersShortcut = raw.providersShortcut,
                safetyWarning = raw.safetyWarning ?: false,
                result = result,
                parts = nodeParts.takeIf { it.isNotEmpty() },
                mode = mode
            )
        }

        return DiagnosticTree(
            templateId = rawTree.templateId,
            version = rawTree.version,
            locale = rawTree.locale,
            root = rawTree.root,
            nodes = nodes
        )
    }

    // ---------- Helpers ----------

    private fun AssetsDiagnosticDataSource.PartDetailRaw.toDomain(): PartDetail =
        PartDetail(
            id = id,
            product = product,
            code = code,
            features = features,
            supplier = supplier,
            technicalContacts = technicalContacts,
            imageResName = imageResName
        )

    private fun mapResult(raw: String): EndResult? =
        when (raw.uppercase()) {
            "RESOLVED" -> EndResult.RESOLVED
            "NO_ISSUE" -> EndResult.NO_ISSUE
            "COMPONENT_FAULT" -> EndResult.COMPONENT_FAULT
            else -> null
        }
}
