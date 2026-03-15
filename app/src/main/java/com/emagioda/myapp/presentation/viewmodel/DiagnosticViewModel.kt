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
    val errorResId: Int? = null,
    val safetyWarningDismissed: Boolean = false,
    val isBackNavigation: Boolean = false // <-- NUEVO: Bandera de dirección
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
                        errorResId = null,
                        isBackNavigation = false
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

    fun dismissSafetyWarning() {
        uiState = uiState.copy(safetyWarningDismissed = true)
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
        publish(nodesById[localTree.root], isBack = false)
    }

    fun goBack() {
        if (path.size <= 1) return

        if (path.size <= 1) {
            val localTree = tree ?: return
            currentNodeId = localTree.root
            publish(nodesById[currentNodeId], isBack = true)
            return
        }

        path.removeAt(path.lastIndex)
        val localTree = tree ?: return
        val previousId = path.lastOrNull() ?: localTree.root
        currentNodeId = previousId
        publish(nodesById[previousId], isBack = true) // <-- IMPORTANTE: isBack = true
    }

    fun canGoBack(): Boolean = path.size > 1

    private fun goTo(nextId: String?) {
        if (nextId.isNullOrBlank()) return
        if (tree == null) return

        val nextNode = nodesById[nextId]

        if (nextNode == null) {
            publish(current = null, isBack = false)
            return
        }

        path.add(nextNode.id)
        currentNodeId = nextId
        publish(nextNode, isBack = false) // <-- Avanzar: isBack = false
    }

    private fun publish(current: DiagnosticNode?, isBack: Boolean) {
        uiState = uiState.copy(
            current = current,
            path = path.toList(),
            safetyWarningDismissed = false,
            errorResId = if (current == null) R.string.diagnostic_error_loading else null,
            isBackNavigation = isBack // <-- Actualizamos el estado
        )
    }

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
