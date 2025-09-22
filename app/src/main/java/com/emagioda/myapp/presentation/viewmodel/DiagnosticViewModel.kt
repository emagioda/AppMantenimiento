package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emagioda.myapp.domain.model.ActionDetail
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine

data class DiagnosticUiState(
    val machineId: String,
    val tree: DiagnosticTree,
    val current: DiagnosticNode?,     // nodo actual (incluye END sintético si corresponde)
    val path: List<String> = emptyList(), // historial de nodeIds
    val error: String? = null
)

class DiagnosticViewModel(
    private val getTree: GetDiagnosticTreeForMachine,
    private val machineId: String
) : ViewModel() {

    private val tree: DiagnosticTree = getTree(machineId)
    private val nodesById = tree.nodes.associateBy { it.id }

    private var currentNodeId: String = tree.root
    private val path = mutableListOf<String>().apply { add(currentNodeId) }

    // 🔹 Estado observable por Compose
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

    fun continueAction() {
        val n = nodesById[currentNodeId] ?: return
        if (n.type != NodeType.ACTION) return
        goTo(n.next)
    }

    fun restart() {
        path.clear()
        currentNodeId = tree.root
        path.add(currentNodeId)
        publish(nodesById[currentNodeId])
    }

    private fun goTo(nextIdOrEnd: String?) {
        if (nextIdOrEnd.isNullOrBlank()) return

        val nextNode: DiagnosticNode? = when (nextIdOrEnd) {
            "END" -> synthEndNode() // END sintético
            else -> nodesById[nextIdOrEnd]
        }

        // Si es un END sintético, no hay id real que agregar; igual guardamos “END”
        if (nextIdOrEnd == "END") {
            path.add("END")
        } else {
            nextNode?.id?.let { path.add(it) }
        }

        // Actualizamos currentNodeId solo si no es END sintético
        if (nextIdOrEnd != "END") currentNodeId = nextIdOrEnd

        publish(nextNode)
    }

    private fun publish(current: DiagnosticNode?) {
        uiState = uiState.copy(
            current = current,
            path = path.toList(),
            error = if (current == null) "Nodo non trovato" else null
        )
    }

    private fun synthEndNode(): DiagnosticNode =
        DiagnosticNode(
            id = "__END__",
            type = NodeType.END,
            title = "Fine",
            description = "Procedura completata.",
            action = ActionDetail(),
            yes = null,
            no = null,
            next = null
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
