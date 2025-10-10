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
    val providersShortcut: Boolean? = null
)
enum class NodeType { QUESTION, END }