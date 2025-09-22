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
    val yes: String? = null,     // sólo para QUESTION
    val no: String? = null,      // sólo para QUESTION
    val action: ActionDetail? = null, // sólo para ACTION
    val next: String? = null     // ACTION -> siguiente o "END"
)

enum class NodeType { QUESTION, ACTION, END }

data class ActionDetail(
    val steps: List<String>? = null,
    val tools: List<String>? = null,
    val safetyNotes: String? = null
)
