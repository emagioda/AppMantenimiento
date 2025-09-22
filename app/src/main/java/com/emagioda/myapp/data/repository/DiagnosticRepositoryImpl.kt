package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class DiagnosticRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : DiagnosticRepository {

    override fun getTreeForMachine(machineId: String): DiagnosticTree {
        val index = ds.readMachinesIndex()
        val mapping = index.machines.firstOrNull { it.id == machineId }
            ?: error("MachineId no mapeado en machines.json: $machineId")

        val raw = ds.readTemplateRaw(mapping.templateId)

        // Validaciones básicas
        require(raw.nodes.any { it.id == raw.root }) { "Template ${raw.templateId}: root '${raw.root}' no existe" }
        raw.nodes.filter { it.type == "QUESTION" }.forEach { n ->
            require(!n.yes.isNullOrBlank() && !n.no.isNullOrBlank()) { "Nodo ${n.id}: QUESTION sin yes/no" }
        }

        // Mapear al dominio
        val nodes = raw.nodes.map { rn ->
            DiagnosticNode(
                id = rn.id,
                type = when (rn.type) {
                    "QUESTION" -> NodeType.QUESTION
                    "ACTION" -> NodeType.ACTION
                    "END" -> NodeType.END
                    else -> error("Tipo de nodo desconocido: ${rn.type}")
                },
                title = rn.title,
                description = rn.description,
                yes = rn.yes,
                no = rn.no,
                action = rn.action?.let { ra ->
                    ActionDetail(
                        steps = ra.steps,
                        tools = ra.tools,
                        safetyNotes = ra.safetyNotes
                    )
                },
                next = rn.next
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
