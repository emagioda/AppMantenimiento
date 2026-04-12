package com.emagioda.myapp.presentation.screen.history

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.MaintenanceTimelineEvent
import com.emagioda.myapp.presentation.common.MaintenanceEventIcon
import com.emagioda.myapp.presentation.common.MaintenanceCasePrintHelper
import com.emagioda.myapp.presentation.common.MaintenanceResultChip
import com.emagioda.myapp.presentation.common.MaintenanceStatusChip
import com.emagioda.myapp.presentation.common.formatHistoryDateTime
import com.emagioda.myapp.presentation.common.resolveDisplayText
import com.emagioda.myapp.presentation.viewmodel.MaintenanceCaseDetailViewModel
import com.emagioda.myapp.ui.theme.HistoryTimelineLine
import kotlinx.coroutines.launch

private enum class HistoryDetailSheetMode {
    ADD_EVENT,
    RESOLVE_CASE,
    EDIT_CASE,
    REOPEN_CASE,
    CANCEL_CASE
}

private const val MinHistoryUpdateLength = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceCaseDetailScreen(
    caseId: Long,
    onBack: () -> Unit,
    onOpenDiagnosticReference: (String, String) -> Unit
) {
    val context = LocalContext.current
    val vm: MaintenanceCaseDetailViewModel = viewModel(
        factory = MaintenanceCaseDetailViewModel.Factory(
            caseId = caseId,
            observeMaintenanceCaseDetail = ServiceLocator.provideObserveMaintenanceCaseDetail(context),
            addMaintenanceEvent = ServiceLocator.provideAddMaintenanceEvent(context),
            resolveMaintenanceCase = ServiceLocator.provideResolveMaintenanceCase(context),
            updateMaintenanceCase = ServiceLocator.provideUpdateMaintenanceCase(context),
            reopenMaintenanceCase = ServiceLocator.provideReopenMaintenanceCase(context),
            cancelMaintenanceCase = ServiceLocator.provideCancelMaintenanceCase(context)
        )
    )
    val uiState = vm.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var activeSheet by remember { mutableStateOf<HistoryDetailSheetMode?>(null) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val detail = uiState.caseDetail

    LaunchedEffect(uiState.actionErrorResId) {
        uiState.actionErrorResId?.let {
            snackbarHostState.showSnackbar(context.getString(it))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history_case_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (detail != null && detail.status != MaintenanceStatus.CANCELED) {
                        Box {
                            IconButton(onClick = { showActionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.history_actions_menu_cd)
                                )
                            }
                            DropdownMenu(
                                expanded = showActionsMenu,
                                onDismissRequest = { showActionsMenu = false }
                            ) {
                                if (detail.status == MaintenanceStatus.PENDING ||
                                    detail.status == MaintenanceStatus.IN_PROGRESS
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.history_edit_case)) },
                                        onClick = {
                                            showActionsMenu = false
                                            activeSheet = HistoryDetailSheetMode.EDIT_CASE
                                        }
                                    )
                                }
                                if (detail.status == MaintenanceStatus.FINALIZED) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.history_reopen_case)) },
                                        onClick = {
                                            showActionsMenu = false
                                            activeSheet = HistoryDetailSheetMode.REOPEN_CASE
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.history_cancel_case)) },
                                    onClick = {
                                        showActionsMenu = false
                                        activeSheet = HistoryDetailSheetMode.CANCEL_CASE
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorResId != null || uiState.caseDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            uiState.errorResId ?: R.string.history_case_not_found
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { HistoryCaseHeader(detail = detail) }
                    item { HistoryCaseSummary(detail = detail) }
                    item {
                        FilledTonalButton(
                            onClick = {
                                onOpenDiagnosticReference(
                                    detail.machineId,
                                    detail.endNodeId
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.history_open_diagnostic_reference))
                        }
                    }

                    if (detail.status != MaintenanceStatus.FINALIZED &&
                        detail.status != MaintenanceStatus.CANCELED
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { activeSheet = HistoryDetailSheetMode.ADD_EVENT },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isSubmitting
                                ) {
                                    Text(
                                        text = stringResource(R.string.history_add_update),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = { activeSheet = HistoryDetailSheetMode.RESOLVE_CASE },
                                    modifier = Modifier.weight(1f),
                                    enabled = !uiState.isSubmitting
                                ) {
                                    Text(stringResource(R.string.history_finalize_case))
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.history_timeline_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.history_timeline_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    itemsIndexed(
                        items = detail.events,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        TimelineItem(
                            item = item,
                            isLast = index == detail.events.lastIndex
                        )
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                val didStartPrint = MaintenanceCasePrintHelper.printCase(context, detail)
                                if (!didStartPrint) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.history_print_error)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.history_print_case))
                        }
                    }
                }
            }
        }
    }

    if (activeSheet == HistoryDetailSheetMode.ADD_EVENT) {
        AddHistoryEventSheet(
            onDismiss = { activeSheet = null },
            onSubmit = { type, note ->
                vm.addEvent(
                    type = type,
                    title = eventTypeLabel(type, context),
                    note = note
                )
                activeSheet = null
            }
        )
    }

    if (activeSheet == HistoryDetailSheetMode.RESOLVE_CASE) {
        ResolveHistoryCaseSheet(
            onDismiss = { activeSheet = null },
            onSubmit = { note ->
                vm.resolveCase(
                    title = context.getString(R.string.history_event_resolution),
                    note = note
                )
                activeSheet = null
            }
        )
    }

    if (activeSheet == HistoryDetailSheetMode.EDIT_CASE && detail != null) {
        EditHistoryCaseSheet(
            initialProblem = detail.problemSummary.orEmpty(),
            initialTechnicalSummary = detail.diagnosisDescription.orEmpty(),
            onDismiss = { activeSheet = null },
            onSubmit = { problem, technicalSummary ->
                vm.updateCase(
                    problemSummary = problem,
                    technicalSummary = technicalSummary,
                    updateTitle = context.getString(R.string.history_event_case_updated),
                    updateNote = technicalSummary
                )
                activeSheet = null
            }
        )
    }

    if (activeSheet == HistoryDetailSheetMode.REOPEN_CASE) {
        ReopenHistoryCaseSheet(
            onDismiss = { activeSheet = null },
            onSubmit = { note ->
                vm.reopenCase(
                    title = context.getString(R.string.history_event_case_reopened),
                    note = note
                )
                activeSheet = null
            }
        )
    }

    if (activeSheet == HistoryDetailSheetMode.CANCEL_CASE) {
        CancelHistoryCaseSheet(
            onDismiss = { activeSheet = null },
            onSubmit = { reason ->
                vm.cancelCase(
                    title = context.getString(R.string.history_event_case_canceled),
                    reason = reason
                )
                activeSheet = null
            }
        )
    }
}

@Composable
private fun HistoryCaseHeader(
    detail: MaintenanceCaseDetail
) {
    val context = LocalContext.current
    val problemTitle = resolveDisplayText(
        context,
        detail.problemSummary?.takeIf { it.isNotBlank() } ?: detail.diagnosisTitle
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = detail.machineNameSnapshot,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Text(
                text = detail.machineId,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
                MaintenanceStatusChip(status = detail.status)
            }

            Text(
                text = problemTitle,
                style = MaterialTheme.typography.headlineSmall
            )

            MaintenanceResultChip(result = detail.endResult)
        }
    }
}

@Composable
private fun HistoryCaseSummary(
    detail: MaintenanceCaseDetail
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.history_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SummaryCodeLine(
                label = stringResource(R.string.history_case_code),
                value = detail.caseCode
            )
            SummaryLine(
                label = stringResource(R.string.history_detected_at),
                value = formatHistoryDateTime(detail.openedAt)
            )
            SummaryLine(
                label = stringResource(R.string.history_updated_at),
                value = formatHistoryDateTime(detail.updatedAt)
            )
            detail.resolvedAt?.let {
                SummaryLine(
                    label = stringResource(R.string.history_resolved_at),
                    value = formatHistoryDateTime(it)
                )
            }
            detail.canceledAt?.let {
                SummaryLine(
                    label = stringResource(R.string.history_canceled_at),
                    value = formatHistoryDateTime(it)
                )
            }
            detail.cancellationReason?.takeIf { it.isNotBlank() }?.let {
                SummaryLine(
                    label = stringResource(R.string.history_cancellation_reason),
                    value = it
                )
            }
        }
    }
}

@Composable
private fun SummaryCodeLine(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TimelineItem(
    item: MaintenanceTimelineEvent,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MaintenanceEventIcon(type = item.type)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(HistoryTimelineLine)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatHistoryDateTime(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.note?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHistoryEventSheet(
    onDismiss: () -> Unit,
    onSubmit: (MaintenanceEventType, String?) -> Unit
) {
    var selectedTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    var showNoteError by rememberSaveable { mutableStateOf(false) }
    var isTypeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var shouldFocusNoteField by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val noteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedType = selectedTypeName?.let(MaintenanceEventType::valueOf)
    val trimmedNote = note.trim()
    val isNoteValid = trimmedNote.length >= MinHistoryUpdateLength

    LaunchedEffect(selectedTypeName, shouldFocusNoteField) {
        if (shouldFocusNoteField && selectedType != null) {
            noteFocusRequester.requestFocus()
            keyboardController?.show()
            shouldFocusNoteField = false
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isTypeMenuExpanded) {
                            Modifier.blur(10.dp)
                        } else {
                            Modifier
                        }
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_event_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(R.string.history_event_sheet_type_label),
                    style = MaterialTheme.typography.labelLarge
                )

                EventTypeSelector(
                    selectedType = selectedType,
                    expanded = isTypeMenuExpanded,
                    onExpandedChange = { isTypeMenuExpanded = it }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                        if (showNoteError && it.trim().length >= MinHistoryUpdateLength) {
                            showNoteError = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(noteFocusRequester),
                    label = { Text(stringResource(R.string.history_event_sheet_note_label)) },
                    placeholder = { Text(stringResource(R.string.history_event_sheet_note_hint)) },
                    supportingText = {
                        if (showNoteError) {
                            Text(stringResource(R.string.history_event_sheet_note_min_length))
                        }
                    },
                    isError = showNoteError,
                    minLines = 3
                )

                Button(
                    onClick = {
                        if (selectedType == null) {
                            isTypeMenuExpanded = true
                            return@Button
                        }
                        if (!isNoteValid) {
                            showNoteError = true
                            return@Button
                        }
                        onSubmit(
                            selectedType,
                            trimmedNote
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.history_event_sheet_confirm))
                }

                Spacer(Modifier.height(12.dp))
            }

            if (isTypeMenuExpanded) {
                CenteredEventTypePickerOverlay(
                    selectedType = selectedType,
                    onDismiss = { isTypeMenuExpanded = false },
                    onSelect = { type ->
                        selectedTypeName = type.name
                        isTypeMenuExpanded = false
                        shouldFocusNoteField = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResolveHistoryCaseSheet(
    onDismiss: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var note by rememberSaveable { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.history_resolution_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_resolution_sheet_note_label)) },
                placeholder = { Text(stringResource(R.string.history_resolution_sheet_note_hint)) },
                minLines = 4
            )

            Button(
                onClick = { onSubmit(note.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.history_resolution_sheet_confirm))
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditHistoryCaseSheet(
    initialProblem: String,
    initialTechnicalSummary: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String?) -> Unit
) {
    var problem by rememberSaveable { mutableStateOf(initialProblem) }
    var technicalSummary by rememberSaveable { mutableStateOf(initialTechnicalSummary) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.history_edit_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = problem,
                onValueChange = { problem = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_edit_problem_label)) },
                placeholder = { Text(stringResource(R.string.history_edit_problem_hint)) },
                minLines = 3
            )

            OutlinedTextField(
                value = technicalSummary,
                onValueChange = { technicalSummary = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_edit_technical_label)) },
                placeholder = { Text(stringResource(R.string.history_edit_technical_hint)) },
                minLines = 3
            )

            Button(
                onClick = {
                    val trimmedProblem = problem.trim()
                    if (trimmedProblem.isNotBlank()) {
                        onSubmit(trimmedProblem, technicalSummary.trim().ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = problem.isNotBlank()
            ) {
                Text(stringResource(R.string.history_edit_confirm))
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReopenHistoryCaseSheet(
    onDismiss: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var note by rememberSaveable { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.history_reopen_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_reopen_sheet_note_label)) },
                placeholder = { Text(stringResource(R.string.history_reopen_sheet_note_hint)) },
                minLines = 3
            )

            Button(
                onClick = { onSubmit(note.trim().ifBlank { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.history_reopen_confirm))
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CancelHistoryCaseSheet(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.history_cancel_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.history_cancel_sheet_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = acknowledged,
                    onCheckedChange = { acknowledged = it }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.history_cancel_irreversible_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.history_cancel_access_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.history_cancel_sheet_reason_label)) },
                placeholder = { Text(stringResource(R.string.history_cancel_sheet_reason_hint)) },
                minLines = 3
            )

            Button(
                onClick = {
                    val trimmedReason = reason.trim()
                    if (trimmedReason.isNotBlank() && acknowledged) {
                        onSubmit(trimmedReason)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = reason.isNotBlank() && acknowledged
            ) {
                Text(stringResource(R.string.history_cancel_confirm))
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EventTypeSelector(
    selectedType: MaintenanceEventType?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedType?.let { stringResource(eventTypeRes(it)) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(stringResource(R.string.history_event_sheet_type_placeholder))
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            leadingIcon = selectedType?.let { type ->
                {
                    Icon(
                        imageVector = eventTypeIcon(type),
                        contentDescription = null
                    )
                }
            },
            maxLines = 1,
            textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onExpandedChange(!expanded) }
        )
    }
}

@Composable
private fun CenteredEventTypePickerOverlay(
    selectedType: MaintenanceEventType?,
    onDismiss: () -> Unit,
    onSelect: (MaintenanceEventType) -> Unit
) {
    val options = listOf(
        MaintenanceEventType.TECHNICIAN_CONTACTED,
        MaintenanceEventType.COMPONENT_REPLACED,
        MaintenanceEventType.TEST_PERFORMED,
        MaintenanceEventType.OBSERVATION,
        MaintenanceEventType.OTHER
    )
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = dismissInteractionSource,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = contentInteractionSource,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 10.dp,
            shadowElevation = 22.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                options.forEachIndexed { index, type ->
                    val isSelected = selectedType == type
                    Surface(
                        onClick = { onSelect(type) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                top = if (index == 0) 10.dp else 6.dp,
                                bottom = if (index == options.lastIndex) 10.dp else 6.dp
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                        border = if (isSelected) {
                            BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        } else {
                            null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = eventTypeIcon(type),
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = stringResource(eventTypeRes(type)),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun eventTypeLabel(
    type: MaintenanceEventType,
    context: Context
): String = context.getString(eventTypeRes(type))

private fun eventTypeIcon(type: MaintenanceEventType) =
    when (type) {
        MaintenanceEventType.PROBLEM_DETECTED -> Icons.Filled.Info
        MaintenanceEventType.TECHNICIAN_CONTACTED -> Icons.Filled.Engineering
        MaintenanceEventType.COMPONENT_REPLACED -> Icons.Filled.Build
        MaintenanceEventType.TEST_PERFORMED -> Icons.Filled.Construction
        MaintenanceEventType.OBSERVATION -> Icons.Filled.Info
        MaintenanceEventType.OTHER -> Icons.Filled.Flag
        MaintenanceEventType.RESOLUTION -> Icons.Filled.CheckCircle
        MaintenanceEventType.CASE_UPDATED -> Icons.Filled.Info
        MaintenanceEventType.CASE_REOPENED -> Icons.Filled.Info
        MaintenanceEventType.CASE_CANCELED -> Icons.Filled.Info
    }

private fun eventTypeRes(type: MaintenanceEventType): Int =
    when (type) {
        MaintenanceEventType.PROBLEM_DETECTED -> R.string.history_event_problem
        MaintenanceEventType.TECHNICIAN_CONTACTED -> R.string.history_event_technician
        MaintenanceEventType.COMPONENT_REPLACED -> R.string.history_event_component
        MaintenanceEventType.TEST_PERFORMED -> R.string.history_event_test
        MaintenanceEventType.OBSERVATION -> R.string.history_event_observation
        MaintenanceEventType.OTHER -> R.string.history_event_other
        MaintenanceEventType.RESOLUTION -> R.string.history_event_resolution
        MaintenanceEventType.CASE_UPDATED -> R.string.history_event_case_updated
        MaintenanceEventType.CASE_REOPENED -> R.string.history_event_case_reopened
        MaintenanceEventType.CASE_CANCELED -> R.string.history_event_case_canceled
    }
