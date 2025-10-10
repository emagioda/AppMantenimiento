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

        require(raw.nodes.any { it.id == raw.root }) { "Template ${raw.templateId}: root '${raw.root}' no existe" }
        raw.nodes.filter { it.type == "QUESTION" }.forEach { n ->
            require(!n.yes.isNullOrBlank() && !n.no.isNullOrBlank()) { "Nodo ${n.id}: QUESTION sin yes/no" }
        }

        val nodes = raw.nodes.map { rn ->
            DiagnosticNode(
                id = rn.id,
                type = when (rn.type) {
                    "QUESTION" -> NodeType.QUESTION
                    "END" -> NodeType.END
                    else -> error("Tipo de nodo desconocido: ${rn.type}")
                },
                title = rn.title,
                description = rn.description,
                yes = rn.yes,
                no = rn.no,
                providersShortcut = rn.providersShortcut // 👈 NUEVO
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
