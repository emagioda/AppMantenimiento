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

        require(raw.nodes.any { it.id == raw.root }) {
            "Template ${raw.templateId}: root '${raw.root}' no existe"
        }

        raw.nodes.filter { it.type == "QUESTION" }.forEach { n ->
            require(!n.yes.isNullOrBlank()) { "Nodo ${n.id}: QUESTION sin yes" }
            // IMPORTANTE: ahora permitimos no tener "no" para CONTINUE_ONLY
            // allow missing "no" when mode == CONTINUE_ONLY
        }

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
                parts = rn.parts?.map { pr -> PartRef(pr.id, pr.qty, pr.note) },

                // NUEVO: modo de la pregunta
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
