package com.emagioda.myapp.domain.model

// ------------------------------------
// Expanded part with catalog data
// ------------------------------------
data class PartDetail(
    val id: String,
    val product: String,
    val code: String?,
    val features: String?,
    val supplier: String?,
    val technicalContacts: String?,
    val imageResName: String?
)

// Part reference inside END node
data class PartRefResolved(
    val detail: PartDetail,
    val qty: Int?
)

// ------------------------------------
// Diagnostic tree
// ------------------------------------
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
    val providersShortcut: Boolean? = null,
    val result: EndResult? = null,
    val parts: List<PartRefResolved>? = null,
    val mode: QuestionMode = QuestionMode.YES_NO
)

enum class NodeType { QUESTION, END }

enum class EndResult { RESOLVED, NO_ISSUE, COMPONENT_FAULT }

enum class QuestionMode { YES_NO, CONTINUE_ONLY }
