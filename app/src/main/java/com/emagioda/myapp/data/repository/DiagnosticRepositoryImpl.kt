package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class DiagnosticRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : DiagnosticRepository {

    override fun getTreeForMachine(machineId: String): DiagnosticTree {
        // 1) Buscar máquina -> template
        val machinesIndex = ds.readMachinesIndex()
        val machine = machinesIndex.machines.firstOrNull { it.id == machineId }
            ?: error("Machine not found for id=$machineId")

        // 2) Cargar árbol crudo + catálogo de piezas
        val rawTree = ds.readTemplateRaw(machine.templateId)
        val partsCatalog = ds.readPartsCatalog()

        // 3) Mapear nodos crudos -> nodos de dominio
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

            // Piezas definidas inline en el propio nodo (si algún día las usas)
            val inlineParts = raw.parts.orEmpty().mapNotNull { ref ->
                val detailRaw = partsCatalog.parts.firstOrNull { it.id == ref.id }
                    ?: return@mapNotNull null

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
                result = result,
                parts = inlineParts.takeIf { it.isNotEmpty() },
                mode = mode
            )
        }

        return DiagnosticTree(
            templateId = rawTree.templateId ?: machine.templateId,
            version = rawTree.version ?: 1,
            locale = rawTree.locale ?: "it",
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
