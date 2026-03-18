package com.emagioda.myapp.presentation.screen.diagnostic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.InitialMaintenanceAction
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.model.QuestionMode
import com.emagioda.myapp.domain.model.SchematicDocument
import com.emagioda.myapp.presentation.common.SafetyWarningDialog
import com.emagioda.myapp.presentation.common.SchematicPdfOpener
import com.emagioda.myapp.presentation.common.resolveDisplayText
import com.emagioda.myapp.presentation.screen.diagnostic.components.DiagnosticPartsSection
import com.emagioda.myapp.presentation.viewmodel.DiagnosticUiState
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel
import com.emagioda.myapp.ui.theme.ResultFaultRed
import com.emagioda.myapp.ui.theme.ResultResolvedGreen
import com.emagioda.myapp.ui.theme.ResultWarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenTechnicians: () -> Unit,
    onOpenFilteredContacts: (String, String) -> Unit = { _, _ -> },
    onOpenHistoryCase: (Long) -> Unit
) {
    val context = LocalContext.current
    val vm: DiagnosticViewModel = viewModel(
        factory = DiagnosticViewModel.Factory(
            getTree = ServiceLocator.provideGetTreeUseCase(context),
            getMachineDetail = ServiceLocator.provideGetMachineDetail(context),
            createMaintenanceCase = ServiceLocator.provideCreateMaintenanceCase(context),
            machineId = machineId
        )
    )
    val uiState = vm.uiState
    val node = uiState.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val showSafetyDialog = (node?.safetyWarning == true) && !uiState.safetyWarningDismissed

    fun openSchematic(document: SchematicDocument) {
        val didOpen = SchematicPdfOpener.open(context, document)
        if (!didOpen) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.diagnostic_schematic_open_error)
                )
            }
        }
    }

    LaunchedEffect(uiState.saveCaseErrorResId) {
        uiState.saveCaseErrorResId?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            vm.dismissSaveError()
        }
    }

    BackHandler(enabled = vm.canGoBack()) { vm.goBack() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.diagnostic_title)) },
                navigationIcon = {
                    if (vm.canGoBack()) {
                        IconButton(onClick = { vm.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorResId != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(uiState.errorResId),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                node == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.diagnostic_error_loading),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                else -> {
                    when (node.type) {
                        NodeType.QUESTION -> QuestionContent(
                            node = node,
                            isBack = uiState.isBackNavigation,
                            vm = vm
                        )

                        NodeType.END -> EndContent(
                            node = node,
                            uiState = uiState,
                            vm = vm,
                            onRestartToHome = onRestartToHome,
                            onOpenTechnicians = onOpenTechnicians,
                            onOpenFilteredContacts = onOpenFilteredContacts,
                            onOpenSchematic = ::openSchematic,
                            onOpenHistoryCase = onOpenHistoryCase
                        )
                    }
                }
            }

            if (showSafetyDialog) {
                SafetyWarningDialog(
                    onConfirm = { vm.dismissSafetyWarning() }
                )
            }
        }
    }
}

private data class DiagnosticReferenceUiState(
    val machineName: String? = null,
    val node: DiagnosticNode? = null,
    val isLoading: Boolean = true,
    val errorResId: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticEndReferenceScreen(
    machineId: String,
    nodeId: String,
    onBack: () -> Unit,
    onOpenTechnicians: () -> Unit,
    onOpenFilteredContacts: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var uiState by remember(machineId, nodeId) {
        mutableStateOf(DiagnosticReferenceUiState())
    }
    var safetyWarningDismissed by remember(nodeId) { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val node = uiState.node
    val showSafetyDialog = (node?.safetyWarning == true) && !safetyWarningDismissed

    fun openSchematic(document: SchematicDocument) {
        val didOpen = SchematicPdfOpener.open(context, document)
        if (!didOpen) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.diagnostic_schematic_open_error)
                )
            }
        }
    }

    LaunchedEffect(machineId, nodeId) {
        uiState = DiagnosticReferenceUiState(isLoading = true)
        safetyWarningDismissed = false

        val loadedState = withContext(Dispatchers.IO) {
            runCatching {
                val tree = ServiceLocator.provideGetTreeUseCase(context)(machineId)
                val machine = ServiceLocator.provideGetMachineDetail(context)(machineId)
                val endNode = tree.nodes.firstOrNull { it.id == nodeId && it.type == NodeType.END }

                if (endNode == null) {
                    DiagnosticReferenceUiState(
                        isLoading = false,
                        errorResId = R.string.diagnostic_error_loading
                    )
                } else {
                    DiagnosticReferenceUiState(
                        machineName = machine?.name ?: machineId,
                        node = endNode,
                        isLoading = false,
                        errorResId = null
                    )
                }
            }.getOrElse {
                DiagnosticReferenceUiState(
                    isLoading = false,
                    errorResId = R.string.diagnostic_error_loading
                )
            }
        }

        uiState = loadedState
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.diagnostic_reference_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorResId != null || node == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(uiState.errorResId ?: R.string.diagnostic_error_loading),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                else -> {
                    DiagnosticEndReferenceContent(
                        node = node,
                        onOpenTechnicians = onOpenTechnicians,
                        onOpenFilteredContacts = onOpenFilteredContacts,
                        onOpenSchematic = ::openSchematic
                    )
                }
            }

            if (showSafetyDialog) {
                SafetyWarningDialog(
                    onConfirm = { safetyWarningDismissed = true }
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(
    node: DiagnosticNode,
    isBack: Boolean,
    vm: DiagnosticViewModel
) {
    AnimatedContent(
        targetState = node to isBack,
        transitionSpec = {
            val (_, back) = targetState
            if (back) {
                (slideInHorizontally { -it } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally { it } + fadeOut(tween(300))
                )
            } else {
                (slideInHorizontally { it } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally { -it } + fadeOut(tween(300))
                )
            }
        },
        label = "question-transition"
    ) { (targetNode, _) ->
        val context = LocalContext.current
        val titleText = remember(targetNode.title, context.packageName) {
            resolveDisplayText(context, targetNode.title)
        }
        val descriptionText = remember(targetNode.description, context.packageName) {
            targetNode.description?.let { resolveDisplayText(context, it) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CardDefaults.elevatedShape
                        ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        descriptionText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttonHeight = 56.dp
                val buttonShape = MaterialTheme.shapes.medium

                when (targetNode.mode) {
                    QuestionMode.CONTINUE_ONLY -> {
                        Button(
                            onClick = vm::answerYes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            shape = buttonShape
                        ) {
                            Text(
                                stringResource(R.string.diagnostic_continue).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    QuestionMode.YES_NO -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledTonalButton(
                                onClick = vm::answerNo,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight),
                                shape = buttonShape
                            ) {
                                Text(
                                    stringResource(R.string.diagnostic_no).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = vm::answerYes,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(buttonHeight),
                                shape = buttonShape
                            ) {
                                Text(
                                    stringResource(R.string.diagnostic_yes).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticEndReferenceContent(
    node: DiagnosticNode,
    onOpenTechnicians: () -> Unit,
    onOpenFilteredContacts: (String, String) -> Unit,
    onOpenSchematic: (SchematicDocument) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val titleText = remember(node.title, context.packageName) {
        resolveDisplayText(context, node.title)
    }
    val descriptionText = remember(node.description, context.packageName) {
        node.description?.let { resolveDisplayText(context, it) }
    }
    val showTechniciansButton = remember(node.parts) {
        node.parts.orEmpty().none { part ->
            part.detail.supplier.orEmpty().isNotEmpty() ||
                part.detail.technicalContacts.orEmpty().isNotEmpty()
        }
    }

    LaunchedEffect(node.id) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EndResultIcon(node.result ?: EndResult.NO_ISSUE)

            Spacer(Modifier.height(24.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            descriptionText?.let {
                Spacer(Modifier.height(24.dp))
                SuggestCard(it)
            }

            node.schematics?.takeIf { it.isNotEmpty() }?.let { schematics ->
                Spacer(Modifier.height(24.dp))
                SchematicsSection(
                    schematics = schematics,
                    onOpenSchematic = onOpenSchematic
                )
            }

            node.parts?.takeIf { it.isNotEmpty() }?.let { parts ->
                Spacer(Modifier.height(24.dp))
                DiagnosticPartsSection(
                    parts = parts,
                    onContactClick = { providerIds, technicianIds ->
                        onOpenFilteredContacts(
                            providerIds.joinToString(","),
                            technicianIds.joinToString(",")
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            if (showTechniciansButton) {
                FilledTonalButton(
                    onClick = onOpenTechnicians,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.contacts_tech_shortcut).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EndContent(
    node: DiagnosticNode,
    uiState: DiagnosticUiState,
    vm: DiagnosticViewModel,
    onRestartToHome: () -> Unit,
    onOpenTechnicians: () -> Unit,
    onOpenFilteredContacts: (String, String) -> Unit,
    onOpenSchematic: (SchematicDocument) -> Unit,
    onOpenHistoryCase: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val titleText = remember(node.title, context.packageName) {
        resolveDisplayText(context, node.title)
    }
    val descriptionText = remember(node.description, context.packageName) {
        node.description?.let { resolveDisplayText(context, it) }
    }
    val showTechniciansButton = remember(node.parts) {
        node.parts.orEmpty().none { part ->
            part.detail.supplier.orEmpty().isNotEmpty() ||
                part.detail.technicalContacts.orEmpty().isNotEmpty()
        }
    }
    var showSaveSheet by remember(node.id) { mutableStateOf(false) }

    LaunchedEffect(node.id) {
        scrollState.scrollTo(0)
    }

    LaunchedEffect(uiState.savedCaseId) {
        if (uiState.savedCaseId != null) {
            showSaveSheet = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EndResultIcon(node.result ?: EndResult.NO_ISSUE)

            Spacer(Modifier.height(24.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            descriptionText?.let {
                Spacer(Modifier.height(24.dp))
                SuggestCard(it)
            }

            node.schematics?.takeIf { it.isNotEmpty() }?.let { schematics ->
                Spacer(Modifier.height(24.dp))
                SchematicsSection(
                    schematics = schematics,
                    onOpenSchematic = onOpenSchematic
                )
            }

            node.parts?.takeIf { it.isNotEmpty() }?.let { parts ->
                Spacer(Modifier.height(24.dp))
                DiagnosticPartsSection(
                    parts = parts,
                    onContactClick = { providerIds, technicianIds ->
                        onOpenFilteredContacts(
                            providerIds.joinToString(","),
                            technicianIds.joinToString(",")
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            MaintenanceFollowUpSection(
                node = node,
                uiState = uiState,
                vm = vm,
                showSaveSheet = showSaveSheet,
                onShowSheet = { showSaveSheet = true },
                onDismissSheet = { showSaveSheet = false },
                onOpenHistoryCase = onOpenHistoryCase,
                titleText = titleText,
                descriptionText = descriptionText
            )

            Spacer(Modifier.height(24.dp))

            if (showTechniciansButton) {
                FilledTonalButton(
                    onClick = onOpenTechnicians,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(R.string.contacts_tech_shortcut).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledIconButton(
                    onClick = {
                        vm.restart()
                        onRestartToHome()
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = stringResource(R.string.diagnostic_home),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.diagnostic_home),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MaintenanceFollowUpSection(
    node: DiagnosticNode,
    uiState: DiagnosticUiState,
    vm: DiagnosticViewModel,
    showSaveSheet: Boolean,
    onShowSheet: () -> Unit,
    onDismissSheet: () -> Unit,
    onOpenHistoryCase: (Long) -> Unit,
    titleText: String,
    descriptionText: String?
) {
    val savedCaseId = uiState.savedCaseId
    val problemDetectedTitle = stringResource(R.string.history_event_problem)
    val resolutionTitle = stringResource(R.string.history_event_resolution)
    val technicianTitle = stringResource(R.string.history_event_technician)
    val componentTitle = stringResource(R.string.history_event_component)
    val testTitle = stringResource(R.string.history_event_test)
    val otherTitle = stringResource(R.string.history_event_other)

    if (savedCaseId == null) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_save_prompt_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.history_save_prompt_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onShowSheet,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.history_save_button),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.history_saved_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.history_saved_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = { onOpenHistoryCase(savedCaseId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.history_view_case))
                }
            }
        }
    }

    if (showSaveSheet) {
        SaveDiagnosticBottomSheet(
            machineName = uiState.machineName ?: uiState.machineId,
            diagnosisTitle = titleText,
            diagnosisDescription = descriptionText,
            suggestedStatus = suggestedStatusFor(node.result),
            isSaving = uiState.isSavingCase,
            onDismiss = onDismissSheet,
            onSave = { status, problemNote, action, actionNote ->
                vm.saveCurrentDiagnosis(
                    status = status,
                    problemNote = problemNote,
                    initialAction = action,
                    initialActionNote = actionNote,
                    problemTitle = problemDetectedTitle,
                    initialActionTitle = when (action) {
                        InitialMaintenanceAction.NONE -> null
                        InitialMaintenanceAction.TECHNICIAN_CONTACTED -> technicianTitle
                        InitialMaintenanceAction.COMPONENT_REPLACED -> componentTitle
                        InitialMaintenanceAction.TEST_PERFORMED -> testTitle
                        InitialMaintenanceAction.OTHER -> otherTitle
                    },
                    autoResolutionTitle = if (status == MaintenanceStatus.FINALIZED) {
                        resolutionTitle
                    } else {
                        null
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SaveDiagnosticBottomSheet(
    machineName: String,
    diagnosisTitle: String,
    diagnosisDescription: String?,
    suggestedStatus: MaintenanceStatus,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (MaintenanceStatus, String, InitialMaintenanceAction, String) -> Unit
) {
    var selectedStatus by rememberSaveable { mutableStateOf(suggestedStatus.name) }
    var problemNote by rememberSaveable { mutableStateOf("") }
    var initialAction by rememberSaveable { mutableStateOf(InitialMaintenanceAction.NONE.name) }
    var actionNote by rememberSaveable { mutableStateOf("") }
    var showProblemNoteError by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val problemNoteBringIntoViewRequester = remember { BringIntoViewRequester() }
    val actionNoteBringIntoViewRequester = remember { BringIntoViewRequester() }
    val selectedInitialAction = InitialMaintenanceAction.valueOf(initialAction)

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = machineName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = diagnosisTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                            diagnosisDescription?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.history_sheet_status),
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusFilterChip(
                            selected = selectedStatus == MaintenanceStatus.PENDING.name,
                            label = stringResource(R.string.history_status_pending),
                            onClick = { selectedStatus = MaintenanceStatus.PENDING.name }
                        )
                        StatusFilterChip(
                            selected = selectedStatus == MaintenanceStatus.IN_PROGRESS.name,
                            label = stringResource(R.string.history_status_in_progress),
                            onClick = { selectedStatus = MaintenanceStatus.IN_PROGRESS.name }
                        )
                        StatusFilterChip(
                            selected = selectedStatus == MaintenanceStatus.FINALIZED.name,
                            label = stringResource(R.string.history_status_finalized),
                            onClick = { selectedStatus = MaintenanceStatus.FINALIZED.name }
                        )
                    }

                    OutlinedTextField(
                        value = problemNote,
                        onValueChange = {
                            problemNote = it
                            if (it.trim().isNotBlank()) {
                                showProblemNoteError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(problemNoteBringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        problemNoteBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        label = { Text(stringResource(R.string.history_sheet_problem_label)) },
                        placeholder = { Text(stringResource(R.string.history_sheet_problem_hint)) },
                        supportingText = {
                            if (showProblemNoteError) {
                                Text(stringResource(R.string.history_sheet_problem_required))
                            }
                        },
                        isError = showProblemNoteError,
                        minLines = 3
                    )

                    Text(
                        text = stringResource(R.string.history_sheet_first_action_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = stringResource(R.string.history_sheet_first_action_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    InitialActionSelector(
                        selectedAction = selectedInitialAction,
                        onSelect = { initialAction = it.name }
                    )

                    if (selectedInitialAction != InitialMaintenanceAction.NONE) {
                        OutlinedTextField(
                            value = actionNote,
                            onValueChange = { actionNote = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(actionNoteBringIntoViewRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            actionNoteBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            label = { Text(stringResource(R.string.history_sheet_action_detail_label)) },
                            placeholder = { Text(stringResource(R.string.history_sheet_action_detail_hint)) },
                            minLines = 2
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text(stringResource(R.string.history_sheet_cancel))
                }

                Button(
                    onClick = {
                        val trimmedProblemNote = problemNote.trim()
                        if (trimmedProblemNote.isBlank()) {
                            showProblemNoteError = true
                            return@Button
                        }
                        onSave(
                            MaintenanceStatus.valueOf(selectedStatus),
                            trimmedProblemNote,
                            selectedInitialAction,
                            actionNote
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.history_sheet_confirm))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun InitialActionSelector(
    selectedAction: InitialMaintenanceAction,
    onSelect: (InitialMaintenanceAction) -> Unit
) {
    val options = listOf(
        InitialMaintenanceAction.NONE,
        InitialMaintenanceAction.TECHNICIAN_CONTACTED,
        InitialMaintenanceAction.COMPONENT_REPLACED,
        InitialMaintenanceAction.TEST_PERFORMED,
        InitialMaintenanceAction.OTHER
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { action ->
            FilterChip(
                selected = selectedAction == action,
                onClick = { onSelect(action) },
                label = {
                    Text(
                        text = when (action) {
                            InitialMaintenanceAction.NONE -> stringResource(R.string.history_initial_action_none)
                            InitialMaintenanceAction.TECHNICIAN_CONTACTED -> stringResource(R.string.history_event_technician)
                            InitialMaintenanceAction.COMPONENT_REPLACED -> stringResource(R.string.history_event_component)
                            InitialMaintenanceAction.TEST_PERFORMED -> stringResource(R.string.history_event_test)
                            InitialMaintenanceAction.OTHER -> stringResource(R.string.history_event_other)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                }
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun SchematicsSection(
    schematics: List<SchematicDocument>,
    onOpenSchematic: (SchematicDocument) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.diagnostic_schematics_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.diagnostic_schematics_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        schematics.forEachIndexed { index, schematic ->
            FilledTonalButton(
                onClick = { onOpenSchematic(schematic) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = schematic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (index < schematics.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EndResultIcon(result: EndResult) {
    val (bg, icon) = when (result) {
        EndResult.RESOLVED -> ResultResolvedGreen to Icons.Filled.Check
        EndResult.NO_ISSUE -> ResultWarningAmber to Icons.Filled.Warning
        EndResult.COMPONENT_FAULT -> ResultFaultRed to Icons.Filled.Build
    }

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun SuggestCard(text: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CardDefaults.elevatedShape
            )
            .animateContentSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.diagnostic_suggestions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun suggestedStatusFor(result: EndResult?): MaintenanceStatus =
    if (result == EndResult.RESOLVED) {
        MaintenanceStatus.FINALIZED
    } else {
        MaintenanceStatus.PENDING
    }
