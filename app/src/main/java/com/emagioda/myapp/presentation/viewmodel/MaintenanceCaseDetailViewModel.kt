package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.CancelMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.CreateMaintenanceEventRequest
import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.ReopenMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.ResolveMaintenanceCaseRequest
import com.emagioda.myapp.domain.model.UpdateMaintenanceCaseRequest
import com.emagioda.myapp.domain.usecase.AddMaintenanceEvent
import com.emagioda.myapp.domain.usecase.CancelMaintenanceCase
import com.emagioda.myapp.domain.usecase.ObserveMaintenanceCaseDetail
import com.emagioda.myapp.domain.usecase.ReopenMaintenanceCase
import com.emagioda.myapp.domain.usecase.ResolveMaintenanceCase
import com.emagioda.myapp.domain.usecase.UpdateMaintenanceCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MaintenanceCaseDetailUiState(
    val caseDetail: MaintenanceCaseDetail? = null,
    val isLoading: Boolean = true,
    val errorResId: Int? = null,
    val isSubmitting: Boolean = false,
    val actionErrorResId: Int? = null
)

class MaintenanceCaseDetailViewModel(
    private val caseId: Long,
    private val observeMaintenanceCaseDetail: ObserveMaintenanceCaseDetail,
    private val addMaintenanceEvent: AddMaintenanceEvent,
    private val resolveMaintenanceCase: ResolveMaintenanceCase,
    private val updateMaintenanceCase: UpdateMaintenanceCase,
    private val reopenMaintenanceCase: ReopenMaintenanceCase,
    private val cancelMaintenanceCase: CancelMaintenanceCase
) : ViewModel() {

    var uiState by mutableStateOf(MaintenanceCaseDetailUiState())
        private set

    init {
        viewModelScope.launch {
            observeMaintenanceCaseDetail(caseId).collectLatest { detail ->
                uiState = uiState.copy(
                    caseDetail = detail,
                    isLoading = false,
                    errorResId = if (detail == null) R.string.history_case_not_found else null
                )
            }
        }
    }

    fun addEvent(
        type: MaintenanceEventType,
        title: String,
        note: String?
    ) {
        submit {
            addMaintenanceEvent(
                CreateMaintenanceEventRequest(
                    caseId = caseId,
                    type = type,
                    title = title,
                    note = note
                )
            )
        }
    }

    fun resolveCase(
        title: String,
        note: String?
    ) {
        submit {
            resolveMaintenanceCase(
                ResolveMaintenanceCaseRequest(
                    caseId = caseId,
                    resolutionTitle = title,
                    resolutionNote = note
                )
            )
        }
    }

    fun updateCase(
        problemSummary: String,
        technicalSummary: String?,
        updateTitle: String,
        updateNote: String?
    ) {
        submit {
            updateMaintenanceCase(
                UpdateMaintenanceCaseRequest(
                    caseId = caseId,
                    problemSummary = problemSummary,
                    technicalSummary = technicalSummary,
                    updateTitle = updateTitle,
                    updateNote = updateNote
                )
            )
        }
    }

    fun reopenCase(
        title: String,
        note: String?
    ) {
        submit {
            reopenMaintenanceCase(
                ReopenMaintenanceCaseRequest(
                    caseId = caseId,
                    reopenTitle = title,
                    reopenNote = note
                )
            )
        }
    }

    fun cancelCase(
        title: String,
        reason: String
    ) {
        submit {
            cancelMaintenanceCase(
                CancelMaintenanceCaseRequest(
                    caseId = caseId,
                    cancelTitle = title,
                    reason = reason
                )
            )
        }
    }

    private fun submit(action: suspend () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, actionErrorResId = null)
            runCatching { action() }
                .onFailure {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        actionErrorResId = R.string.history_action_error
                    )
                }
                .onSuccess {
                    uiState = uiState.copy(
                        isSubmitting = false,
                        actionErrorResId = null
                    )
                }
        }
    }

    class Factory(
        private val caseId: Long,
        private val observeMaintenanceCaseDetail: ObserveMaintenanceCaseDetail,
        private val addMaintenanceEvent: AddMaintenanceEvent,
        private val resolveMaintenanceCase: ResolveMaintenanceCase,
        private val updateMaintenanceCase: UpdateMaintenanceCase,
        private val reopenMaintenanceCase: ReopenMaintenanceCase,
        private val cancelMaintenanceCase: CancelMaintenanceCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MaintenanceCaseDetailViewModel(
                caseId = caseId,
                observeMaintenanceCaseDetail = observeMaintenanceCaseDetail,
                addMaintenanceEvent = addMaintenanceEvent,
                resolveMaintenanceCase = resolveMaintenanceCase,
                updateMaintenanceCase = updateMaintenanceCase,
                reopenMaintenanceCase = reopenMaintenanceCase,
                cancelMaintenanceCase = cancelMaintenanceCase
            ) as T
        }
    }
}
