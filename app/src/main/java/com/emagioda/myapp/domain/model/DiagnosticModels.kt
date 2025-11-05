package com.emagioda.myapp.domain.model

data class DiagnosticTree(
    val templateId: String,
    val version: Int,
    val locale: String,
    val root: String,
    val nodes: List<DiagnosticNode>
)

data class DiagnosticNode(
    val id: String,
    val type: NodeType,
    val title: String,
    val description: String? = null,
    val yes: String? = null,
    val no: String? = null,
    // Mostrar botón “Tecnici di fiducia” al finalizar
    val providersShortcut: Boolean? = null,
    // NUEVO: solo para END
    val result: EndResult? = null,
    val parts: List<PartRef>? = null
)

data class PartRef(
    val id: String,         // ej: "coclea_motore_1ph_0_75kw"
    val qty: Int? = null,
    val note: String? = null
)

enum class EndResult { RESOLVED, NO_ISSUE, COMPONENT_FAULT }

enum class NodeType { QUESTION, END }
