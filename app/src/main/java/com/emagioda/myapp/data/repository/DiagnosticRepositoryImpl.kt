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

        // 3) Precalcular piezas por nodo, usando nodeRefs del catálogo
        val partsByNodeId = buildPartsByNodeId(partsCatalog)

        // 4) Mapear nodos crudos -> nodos de dominio
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

            // Piezas desde catálogo (nodeRefs)
            val catalogParts = partsByNodeId[raw.id].orEmpty()

            // Piezas definidas inline en el propio nodo (si algún día las usas)
            val inlineParts = raw.parts.orEmpty().mapNotNull { ref ->
                val detailRaw = partsCatalog.parts.firstOrNull { it.id == ref.id }
                    ?: return@mapNotNull null

                PartRefResolved(
                    detail = detailRaw.toDomain(),
                    qty = ref.qty
                )
            }

            val combinedParts = (catalogParts + inlineParts).takeIf { it.isNotEmpty() }

            DiagnosticNode(
                id = raw.id,
                type = type,
                title = raw.title,
                description = raw.description,
                yes = raw.yes,
                no = raw.no,
                providersShortcut = raw.providersShortcut,
                result = result,
                parts = combinedParts,
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

    private fun buildPartsByNodeId(
        catalog: AssetsDiagnosticDataSource.PartsCatalog
    ): Map<String, List<PartRefResolved>> {
        val map = mutableMapOf<String, MutableList<PartRefResolved>>()

        catalog.parts.forEach { raw ->
            val detail = raw.toDomain()
            // nodeRefs no tiene cantidad, así que la dejamos null (no se muestra "Cantidad")
            val ref = PartRefResolved(detail = detail, qty = null)

            raw.nodeRefs.forEach { nodeId ->
                map.getOrPut(nodeId) { mutableListOf() }.add(ref)
            }
        }

        return map
    }

    private fun mapResult(raw: String): EndResult? =
        when (raw.uppercase()) {
            "RESOLVED" -> EndResult.RESOLVED
            "NO_ISSUE" -> EndResult.NO_ISSUE
            "COMPONENT_FAULT" -> EndResult.COMPONENT_FAULT
            else -> null
        }
}
