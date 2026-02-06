package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DiagnosticUiState(
    val machineId: String,
    val tree: DiagnosticTree? = null,
    val current: DiagnosticNode?,
    val path: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val errorResId: Int? = null
)

class DiagnosticViewModel(
    private val getTree: GetDiagnosticTreeForMachine,
    private val machineId: String
) : ViewModel() {

    private var tree: DiagnosticTree? = null
    private var nodesById: Map<String, DiagnosticNode> = emptyMap()
    private var currentNodeId: String? = null
    private val path = mutableListOf<String>()

    var uiState by mutableStateOf(
        DiagnosticUiState(
            machineId = machineId,
            current = null
        )
    )
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loadedTree = getTree(machineId)
                val loadedNodes = loadedTree.nodes.associateBy { it.id }
                val rootId = loadedTree.root
                val rootNode = loadedNodes[rootId]
                withContext(Dispatchers.Main) {
                    tree = loadedTree
                    nodesById = loadedNodes
                    currentNodeId = rootId
                    path.clear()
                    path.add(rootId)
                    uiState = uiState.copy(
                        tree = loadedTree,
                        current = rootNode,
                        path = path.toList(),
                        isLoading = false,
                        errorResId = null
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorResId = R.string.diagnostic_error_loading
                    )
                }
            }
        }
    }

    fun answerYes() {
        val currentId = currentNodeId ?: return
        val n = nodesById[currentId] ?: return
        if (n.type != NodeType.QUESTION) return
        goTo(n.yes)
    }

    fun answerNo() {
        val currentId = currentNodeId ?: return
        val n = nodesById[currentId] ?: return
        if (n.type != NodeType.QUESTION) return
        goTo(n.no)
    }

    fun restart() {
        val localTree = tree ?: return
        path.clear()
        currentNodeId = localTree.root
        path.add(localTree.root)
        publish(nodesById[localTree.root])
    }

    fun goBack() {
        if (path.size <= 1) return

        if (path.lastOrNull() == "END") {
            path.removeAt(path.lastIndex)
        }

        if (path.size <= 1) {
            val localTree = tree ?: return
            currentNodeId = localTree.root
            publish(nodesById[currentNodeId])
            return
        }

        path.removeAt(path.lastIndex)
        val localTree = tree ?: return
        val previousId = path.lastOrNull() ?: localTree.root
        currentNodeId = previousId
        publish(nodesById[previousId])
    }

    fun canGoBack(): Boolean = path.size > 1

    private fun goTo(nextIdOrEnd: String?) {
        if (nextIdOrEnd.isNullOrBlank()) return
        if (tree == null) return

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
            errorResId = if (current == null)
                R.string.diagnostic_error_loading
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
