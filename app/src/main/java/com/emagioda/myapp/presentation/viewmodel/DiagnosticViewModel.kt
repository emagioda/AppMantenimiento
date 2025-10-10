package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine

data class DiagnosticUiState(
    val machineId: String,
    val tree: DiagnosticTree,
    val current: DiagnosticNode?,
    val path: List<String> = emptyList(),
    val error: String? = null
)

class DiagnosticViewModel(
    getTree: GetDiagnosticTreeForMachine,
    private val machineId: String
) : ViewModel() {


    private val tree: DiagnosticTree = getTree(machineId)
    private val nodesById = tree.nodes.associateBy { it.id }
    private var currentNodeId: String = tree.root
    private val path = mutableListOf<String>().apply { add(currentNodeId) }

    var uiState by mutableStateOf(
        DiagnosticUiState(
            machineId = machineId,
            tree = tree,
            current = nodesById[currentNodeId],
            path = path.toList()
        )
    )
        private set

    fun answerYes() {
        val n = nodesById[currentNodeId] ?: return
        if (n.type != NodeType.QUESTION) return
        goTo(n.yes)
    }

    fun answerNo() {
        val n = nodesById[currentNodeId] ?: return
        if (n.type != NodeType.QUESTION) return
        goTo(n.no)
    }

    fun restart() {
        path.clear()
        currentNodeId = tree.root
        path.add(currentNodeId)
        publish(nodesById[currentNodeId])
    }

    fun goBack() {
        if (path.size <= 1) return

        if (path.lastOrNull() == "END") {
            path.removeAt(path.lastIndex)
        }

        if (path.size <= 1) {
            currentNodeId = tree.root
            publish(nodesById[currentNodeId])
            return
        }

        path.removeAt(path.lastIndex)
        val previousId = path.lastOrNull() ?: tree.root
        currentNodeId = previousId
        publish(nodesById[previousId])
    }

    fun canGoBack(): Boolean = path.size > 1

    private fun goTo(nextIdOrEnd: String?) {
        if (nextIdOrEnd.isNullOrBlank()) return

        val nextNode: DiagnosticNode? = when (nextIdOrEnd) {
            "END" -> synthEndNode()
            else -> nodesById[nextIdOrEnd]
        }

        if (nextIdOrEnd == "END") {
            path.add("END")
        } else {
            nextNode?.id?.let { path.add(it) }
            currentNodeId = nextIdOrEnd
        }

        publish(nextNode)
    }

    private fun publish(current: DiagnosticNode?) {
        uiState = uiState.copy(
            current = current,
            path = path.toList(),
            error = if (current == null)
                "diagnostic_error_node_not_found"
            else null
        )
    }

    private fun synthEndNode(): DiagnosticNode =
        DiagnosticNode(
            id = "__END__",
            type = NodeType.END,
            title = "diagnostic_end_title",
            description = "diagnostic_end_description",
            yes = null,
            no = null,
        )

    class Factory(
        private val getTree: GetDiagnosticTreeForMachine,
        private val machineId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiagnosticViewModel(getTree, machineId) as T
        }
    }
}
