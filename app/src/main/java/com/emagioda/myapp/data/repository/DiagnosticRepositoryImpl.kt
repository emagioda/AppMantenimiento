package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.AssetContentException
import com.emagioda.myapp.domain.model.ContactRef
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.model.PartDetail
import com.emagioda.myapp.domain.model.PartRefResolved
import com.emagioda.myapp.domain.model.QuestionMode
import com.emagioda.myapp.domain.model.SchematicDocument
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class DiagnosticRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : DiagnosticRepository {

    override fun getTreeForMachine(machineId: String): DiagnosticTree {
        val machine = ds.readMachinesIndex().machines.firstOrNull { it.id == machineId }
            ?: throw AssetContentException("Machine not found for id=$machineId")

        val rawTree = try {
            ds.readTemplateRaw(machine.templateId)
        } catch (e: Exception) {
            throw AssetContentException(
                "Unable to load diagnostic template ${machine.templateId} for $machineId",
                e
            )
        }

        val partsCatalog = try {
            ds.readPartsCatalog()
        } catch (e: Exception) {
            throw AssetContentException("Unable to load parts catalog", e)
        }

        val schematicsCatalog = try {
            ds.readSchematicsCatalog()
        } catch (e: Exception) {
            throw AssetContentException("Unable to load schematics catalog", e)
        }

        validatePartsCatalog(partsCatalog)
        validateSchematicsCatalog(schematicsCatalog)
        validateTree(rawTree, schematicsCatalog)

        val partsMap = partsCatalog.parts.associateBy { it.id }
        val schematicsMap = schematicsCatalog.schematics.associateBy { it.id }

        val nodes = rawTree.nodes.map { raw ->
            val type = raw.type.toNodeType(raw.id)
            val mode = raw.mode.toQuestionMode(raw.id)
            val result = raw.result.toEndResult(raw.id)

            val nodeParts = raw.parts.orEmpty().map { ref ->
                val detailRaw = partsMap[ref.id]
                    ?: throw AssetContentException(
                        "Node ${raw.id} references unknown part ${ref.id}"
                    )

                PartRefResolved(
                    detail = detailRaw.toDomain(),
                    qty = ref.qty
                )
            }

            val nodeSchematics = raw.schematicIds.orEmpty().map { schematicId ->
                val schematicRaw = schematicsMap[schematicId]
                    ?: throw AssetContentException(
                        "Node ${raw.id} references unknown schematic $schematicId"
                    )

                schematicRaw.toDomain()
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
                schematics = nodeSchematics.takeIf { it.isNotEmpty() },
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

    private fun validatePartsCatalog(partsCatalog: AssetsDiagnosticDataSource.PartsCatalog) {
        val duplicateIds = partsCatalog.parts
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicateIds.isNotEmpty()) {
            throw AssetContentException(
                "Duplicate part ids: ${duplicateIds.joinToString()}"
            )
        }

        partsCatalog.parts.forEach { part ->
            if (part.id.isBlank()) {
                throw AssetContentException("Part id cannot be blank")
            }
            part.supplier.orEmpty().forEach { ref ->
                if (ref.id.isBlank()) {
                    throw AssetContentException("Part ${part.id} has blank supplier reference")
                }
            }
            part.technicalContacts.orEmpty().forEach { ref ->
                if (ref.id.isBlank()) {
                    throw AssetContentException(
                        "Part ${part.id} has blank technical contact reference"
                    )
                }
            }
        }
    }

    private fun validateSchematicsCatalog(
        schematicsCatalog: AssetsDiagnosticDataSource.SchematicsCatalog
    ) {
        val duplicateIds = schematicsCatalog.schematics
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicateIds.isNotEmpty()) {
            throw AssetContentException(
                "Duplicate schematic ids: ${duplicateIds.joinToString()}"
            )
        }

        schematicsCatalog.schematics.forEach { schematic ->
            if (schematic.id.isBlank()) {
                throw AssetContentException("Schematic id cannot be blank")
            }
            if (schematic.title.isBlank()) {
                throw AssetContentException("Schematic ${schematic.id} must define a title")
            }
            if (schematic.assetPath.isBlank()) {
                throw AssetContentException("Schematic ${schematic.id} must define an assetPath")
            }
        }
    }

    private fun validateTree(rawTree: AssetsDiagnosticDataSource.RawTree) {
        validateTree(rawTree, null)
    }

    private fun validateTree(
        rawTree: AssetsDiagnosticDataSource.RawTree,
        schematicsCatalog: AssetsDiagnosticDataSource.SchematicsCatalog?
    ) {
        if (rawTree.root.isBlank()) {
            throw AssetContentException("Diagnostic root cannot be blank")
        }

        val duplicateIds = rawTree.nodes
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicateIds.isNotEmpty()) {
            throw AssetContentException(
                "Duplicate diagnostic node ids: ${duplicateIds.joinToString()}"
            )
        }

        val nodeIds = rawTree.nodes.map { it.id }.toSet()
        val schematicIds = schematicsCatalog?.schematics.orEmpty().map { it.id }.toSet()
        if (rawTree.root !in nodeIds) {
            throw AssetContentException("Diagnostic root ${rawTree.root} not found in template")
        }

        rawTree.nodes.forEach { raw ->
            if (raw.id.isBlank()) {
                throw AssetContentException("Diagnostic node id cannot be blank")
            }

            val type = raw.type.toNodeType(raw.id)
            val mode = raw.mode.toQuestionMode(raw.id)
            val result = raw.result.toEndResult(raw.id)

            when (type) {
                NodeType.QUESTION -> {
                    requireRef(raw.id, "yes", raw.yes, nodeIds)
                    if (mode == QuestionMode.YES_NO) {
                        requireRef(raw.id, "no", raw.no, nodeIds)
                    } else if (!raw.no.isNullOrBlank() && raw.no !in nodeIds) {
                        throw AssetContentException(
                            "Node ${raw.id} has invalid optional 'no' reference ${raw.no}"
                        )
                    }
                }

                NodeType.END -> {
                    if (!raw.yes.isNullOrBlank() || !raw.no.isNullOrBlank()) {
                        throw AssetContentException(
                            "End node ${raw.id} cannot define yes/no transitions"
                        )
                    }
                    if (result == null) {
                        throw AssetContentException(
                            "End node ${raw.id} must define a valid result"
                        )
                    }
                }
            }

            raw.parts.orEmpty().forEach { ref ->
                if (ref.id.isBlank()) {
                    throw AssetContentException("Node ${raw.id} contains a blank part reference")
                }
            }

            raw.schematicIds.orEmpty().forEach { schematicId ->
                if (schematicId.isBlank()) {
                    throw AssetContentException("Node ${raw.id} contains a blank schematic id")
                }
                if (schematicIds.isNotEmpty() && schematicId !in schematicIds) {
                    throw AssetContentException(
                        "Node ${raw.id} references unknown schematic $schematicId"
                    )
                }
            }
        }
    }

    private fun requireRef(
        nodeId: String,
        label: String,
        ref: String?,
        validIds: Set<String>
    ) {
        val resolved = ref?.takeIf { it.isNotBlank() }
            ?: throw AssetContentException("Node $nodeId is missing required '$label' transition")

        if (resolved !in validIds) {
            throw AssetContentException(
                "Node $nodeId references unknown $label target $resolved"
            )
        }
    }

    private fun AssetsDiagnosticDataSource.PartDetailRaw.toDomain(): PartDetail =
        PartDetail(
            id = id,
            product = product,
            code = code,
            features = features,
            supplier = supplier?.map { ContactRef(it.id) },
            technicalContacts = technicalContacts?.map { ContactRef(it.id) },
            imageResName = imageResName
        )

    private fun AssetsDiagnosticDataSource.SchematicRaw.toDomain(): SchematicDocument =
        SchematicDocument(
            id = id,
            title = title,
            assetPath = assetPath
        )

    private fun String.toNodeType(nodeId: String): NodeType =
        when (uppercase()) {
            "QUESTION" -> NodeType.QUESTION
            "END" -> NodeType.END
            else -> throw AssetContentException(
                "Node $nodeId has unsupported type '$this'"
            )
        }

    private fun String?.toQuestionMode(nodeId: String): QuestionMode =
        when (this?.uppercase()) {
            "CONTINUE_ONLY" -> QuestionMode.CONTINUE_ONLY
            "YES_NO", null -> QuestionMode.YES_NO
            else -> throw AssetContentException(
                "Node $nodeId has unsupported mode '$this'"
            )
        }

    private fun String?.toEndResult(nodeId: String): EndResult? =
        when (this?.uppercase()) {
            "RESOLVED" -> EndResult.RESOLVED
            "NO_ISSUE" -> EndResult.NO_ISSUE
            "COMPONENT_FAULT" -> EndResult.COMPONENT_FAULT
            null -> null
            else -> throw AssetContentException(
                "Node $nodeId has unsupported result '$this'"
            )
        }
}
