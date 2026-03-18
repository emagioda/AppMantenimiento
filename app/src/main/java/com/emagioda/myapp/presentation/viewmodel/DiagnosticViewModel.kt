package com.emagioda.myapp.presentation.viewmodel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.CreateMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.InitialMaintenanceAction
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.usecase.CreateMaintenanceCase
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine
import com.emagioda.myapp.domain.usecase.GetMachineDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DiagnosticUiState(
    val machineId: String,
    val machineName: String? = null,
    val tree: DiagnosticTree? = null,
    val current: DiagnosticNode? = null,
    val path: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val errorResId: Int? = null,
    val safetyWarningDismissed: Boolean = false,
    val isBackNavigation: Boolean = false,
    val isSavingCase: Boolean = false,
    val saveCaseErrorResId: Int? = null,
    val savedCaseId: Long? = null
)

class DiagnosticViewModel(
    private val getTree: GetDiagnosticTreeForMachine,
    private val getMachineDetail: GetMachineDetail,
    private val createMaintenanceCase: CreateMaintenanceCase,
    private val machineId: String
) : ViewModel() {

    private var tree: DiagnosticTree? = null
    private var nodesById: Map<String, DiagnosticNode> = emptyMap()
    private var currentNodeId: String? = null
    private val path = mutableListOf<String>()

    var uiState by mutableStateOf(
        DiagnosticUiState(machineId = machineId)
    )
        private set

    init {
        load()
    }

    fun dismissSafetyWarning() {
        uiState = uiState.copy(safetyWarningDismissed = true)
    }

    fun dismissSaveError() {
        uiState = uiState.copy(saveCaseErrorResId = null)
    }

    fun answerYes() {
        val currentId = currentNodeId ?: return
        val node = nodesById[currentId] ?: return
        if (node.type != NodeType.QUESTION) return
        goTo(node.yes)
    }

    fun answerNo() {
        val currentId = currentNodeId ?: return
        val node = nodesById[currentId] ?: return
        if (node.type != NodeType.QUESTION) return
        goTo(node.no)
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

        path.removeAt(path.lastIndex)
        val localTree = tree ?: return
        val previousId = path.lastOrNull() ?: localTree.root
        currentNodeId = previousId
        publish(nodesById[previousId], isBack = true)
    }

    fun canGoBack(): Boolean = path.size > 1

    fun saveCurrentDiagnosis(
        status: MaintenanceStatus,
        problemNote: String,
        initialAction: InitialMaintenanceAction,
        initialActionNote: String,
        problemTitle: String,
        initialActionTitle: String?,
        autoResolutionTitle: String?
    ) {
        val node = uiState.current ?: return
        if (node.type != NodeType.END || uiState.savedCaseId != null) return

        uiState = uiState.copy(
            isSavingCase = true,
            saveCaseErrorResId = null
        )

        viewModelScope.launch {
            runCatching {
                createMaintenanceCase(
                    CreateMaintenanceCaseRequest(
                        machineId = machineId,
                        machineNameSnapshot = uiState.machineName ?: machineId,
                        endNodeId = node.id,
                        diagnosisTitle = node.title,
                        diagnosisDescription = node.description,
                        endResult = node.result ?: EndResult.NO_ISSUE,
                        status = status,
                        problemTitle = problemTitle,
                        problemNote = problemNote.trim().takeIf { it.isNotBlank() },
                        initialAction = initialAction,
                        initialActionTitle = initialActionTitle,
                        initialActionNote = initialActionNote.trim().takeIf { it.isNotBlank() },
                        autoResolutionTitle = autoResolutionTitle
                    )
                )
            }.onSuccess { caseId ->
                uiState = uiState.copy(
                    isSavingCase = false,
                    saveCaseErrorResId = null,
                    savedCaseId = caseId
                )
            }.onFailure {
                uiState = uiState.copy(
                    isSavingCase = false,
                    saveCaseErrorResId = R.string.history_action_error
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loadedTree = getTree(machineId)
                val loadedMachine = getMachineDetail(machineId)
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
                        machineName = loadedMachine?.name ?: machineId,
                        tree = loadedTree,
                        current = rootNode,
                        path = path.toList(),
                        isLoading = false,
                        errorResId = null,
                        isBackNavigation = false
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorResId = R.string.diagnostic_error_loading
                    )
                }
            }
        }
    }

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
        publish(nextNode, isBack = false)
    }

    private fun publish(current: DiagnosticNode?, isBack: Boolean) {
        uiState = uiState.copy(
            current = current,
            path = path.toList(),
            safetyWarningDismissed = false,
            errorResId = if (current == null) R.string.diagnostic_error_loading else null,
            isBackNavigation = isBack,
            isSavingCase = false,
            saveCaseErrorResId = null,
            savedCaseId = null
        )
    }

    class Factory(
        private val getTree: GetDiagnosticTreeForMachine,
        private val getMachineDetail: GetMachineDetail,
        private val createMaintenanceCase: CreateMaintenanceCase,
        private val machineId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiagnosticViewModel(
                getTree = getTree,
                getMachineDetail = getMachineDetail,
                createMaintenanceCase = createMaintenanceCase,
                machineId = machineId
            ) as T
        }
    }
}
