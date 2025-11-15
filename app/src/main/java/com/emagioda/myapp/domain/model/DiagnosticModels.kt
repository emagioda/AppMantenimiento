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

    // Solo para END
    val result: EndResult? = null,
    val parts: List<PartRef>? = null,

    // NUEVO: modo de pregunta (YES_NO o CONTINUE_ONLY)
    val mode: QuestionMode = QuestionMode.YES_NO
)

data class PartRef(
    val id: String,
    val qty: Int? = null,
    val note: String? = null
)

enum class EndResult { RESOLVED, NO_ISSUE, COMPONENT_FAULT }

enum class NodeType { QUESTION, END }

// NUEVO
enum class QuestionMode { YES_NO, CONTINUE_ONLY }
