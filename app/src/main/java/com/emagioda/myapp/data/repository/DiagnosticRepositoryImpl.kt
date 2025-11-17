package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class DiagnosticRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : DiagnosticRepository {

    override fun getTreeForMachine(machineId: String): DiagnosticTree {

        // Índice de máquinas
        val index = ds.readMachinesIndex()
        val mapping = index.machines.firstOrNull { it.id == machineId }
            ?: error("MachineId no mapeado: $machineId")

        // Árbol crudo
        val raw = ds.readTemplateRaw(mapping.templateId)

        // Catálogo de repuestos
        val catalog = ds.readPartsCatalog().parts.associateBy { it.id }

        // Mapear nodos
        val nodes = raw.nodes.map { rn ->

            val nodeType = when (rn.type) {
                "QUESTION" -> NodeType.QUESTION
                "END" -> NodeType.END
                else -> error("Tipo de nodo desconocido: ${rn.type}")
            }

            val questionMode = when (rn.mode?.uppercase()) {
                "CONTINUE_ONLY" -> QuestionMode.CONTINUE_ONLY
                "YES_NO", null -> QuestionMode.YES_NO
                else -> error("QuestionMode desconocido: ${rn.mode}")
            }

            val resolvedParts: List<PartRefResolved>? = rn.parts?.map { rawPart ->
                val detailRaw = catalog[rawPart.id]
                    ?: error("Repuesto no encontrado en catálogo: ${rawPart.id}")

                PartRefResolved(
                    detail = PartDetail(
                        id = detailRaw.id,
                        product = detailRaw.product,
                        code = detailRaw.code,
                        features = detailRaw.features,
                        supplier = detailRaw.supplier,
                        technicalContacts = detailRaw.technicalContacts,
                        imageResName = detailRaw.imageResName
                    ),
                    qty = rawPart.qty
                )
            }

            DiagnosticNode(
                id = rn.id,
                type = nodeType,
                title = rn.title,
                description = rn.description,
                yes = rn.yes,
                no = rn.no,
                providersShortcut = rn.providersShortcut,
                result = when (rn.result?.uppercase()) {
                    "RESOLVED" -> EndResult.RESOLVED
                    "NO_ISSUE" -> EndResult.NO_ISSUE
                    "COMPONENT_FAULT" -> EndResult.COMPONENT_FAULT
                    null -> null
                    else -> error("EndResult desconocido: ${rn.result}")
                },
                parts = resolvedParts,
                mode = questionMode
            )
        }

        return DiagnosticTree(
            templateId = raw.templateId,
            version = raw.version,
            locale = raw.locale,
            root = raw.root,
            nodes = nodes
        )
    }
}
