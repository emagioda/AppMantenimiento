package com.emagioda.myapp.presentation.screen.diagnostic

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumPrimaryButton
import com.emagioda.myapp.presentation.common.PremiumScreenBackground
import com.emagioda.myapp.presentation.common.PremiumSecondaryButton
import com.emagioda.myapp.presentation.common.PremiumSectionEyebrow
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

    LaunchedEffect(uiState.savedCaseId) {
        if (uiState.savedCaseId != null) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.history_saved_title),
                duration = SnackbarDuration.Short
            )
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        PremiumScreenBackground(
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
                            pathSize = uiState.path.size,
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
        PremiumScreenBackground(
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
private fun DiagnosticProgressHeader(
    stepCount: Int,
    showLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    val safeStepCount = stepCount.coerceAtLeast(1)
    val dotsScrollState = rememberScrollState()

    LaunchedEffect(safeStepCount) {
        dotsScrollState.animateScrollTo(dotsScrollState.maxValue)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showLabels) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumSectionEyebrow(text = stringResource(R.string.diagnostic_flow_label))
                PremiumSectionEyebrow(
                    text = stringResource(R.string.diagnostic_step_label, stepCount)
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(dotsScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(safeStepCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == safeStepCount - 1) 10.dp else 8.dp)
                        .background(
                            color = if (index == safeStepCount - 1) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(
    node: DiagnosticNode,
    isBack: Boolean,
    pathSize: Int,
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
            DiagnosticProgressHeader(
                stepCount = pathSize,
                showLabels = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                PremiumHeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                ) {
                    Column(
                        modifier = Modifier
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
                when (targetNode.mode) {
                    QuestionMode.CONTINUE_ONLY -> {
                        PremiumPrimaryButton(
                            text = stringResource(R.string.diagnostic_continue).uppercase(),
                            onClick = vm::answerYes,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    QuestionMode.YES_NO -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PremiumSecondaryButton(
                                text = stringResource(R.string.diagnostic_no).uppercase(),
                                onClick = vm::answerNo,
                                modifier = Modifier.weight(1f)
                            )

                            PremiumPrimaryButton(
                                text = stringResource(R.string.diagnostic_yes).uppercase(),
                                onClick = vm::answerYes,
                                modifier = Modifier.weight(1f)
                            )
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
            PremiumHeroCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = endResultAccent(node.result)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EndResultIcon(node.result ?: EndResult.NO_ISSUE)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

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
                PremiumSecondaryButton(
                    text = stringResource(R.string.contacts_tech_shortcut).uppercase(),
                    onClick = onOpenTechnicians,
                    modifier = Modifier.fillMaxWidth()
                )
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
    val defaultDraftStatus = suggestedStatusFor(node.result).name
    fun emptySaveDraft() = SaveDiagnosticDraft(status = defaultDraftStatus)
    var showSaveSheet by remember(node.id) { mutableStateOf(false) }
    var saveDraft by rememberSaveable(
        node.id,
        stateSaver = SaveDiagnosticDraft.Saver
    ) {
        mutableStateOf(emptySaveDraft())
    }

    LaunchedEffect(node.id) {
        scrollState.scrollTo(0)
    }

    LaunchedEffect(uiState.savedCaseId) {
        if (uiState.savedCaseId != null) {
            showSaveSheet = false
            saveDraft = emptySaveDraft()
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
            DiagnosticProgressHeader(
                stepCount = uiState.path.size,
                showLabels = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            PremiumHeroCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = endResultAccent(node.result)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EndResultIcon(node.result ?: EndResult.NO_ISSUE)
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

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
                draft = saveDraft,
                hasDraft = saveDraft.hasContent(defaultStatus = defaultDraftStatus),
                onShowSheet = { showSaveSheet = true },
                onHideSheet = { showSaveSheet = false },
                onDraftChange = { saveDraft = it },
                onDiscardDraft = {
                    saveDraft = emptySaveDraft()
                    showSaveSheet = false
                },
                onOpenHistoryCase = onOpenHistoryCase,
                titleText = titleText,
                descriptionText = descriptionText
            )

            Spacer(Modifier.height(24.dp))

            if (showTechniciansButton) {
                PremiumSecondaryButton(
                    text = stringResource(R.string.contacts_tech_shortcut).uppercase(),
                    onClick = onOpenTechnicians,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }

            PremiumSecondaryButton(
                text = stringResource(R.string.diagnostic_home).uppercase(),
                onClick = {
                    vm.restart()
                    onRestartToHome()
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Filled.Home
            )

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
    draft: SaveDiagnosticDraft,
    hasDraft: Boolean,
    onShowSheet: () -> Unit,
    onHideSheet: () -> Unit,
    onDraftChange: (SaveDiagnosticDraft) -> Unit,
    onDiscardDraft: () -> Unit,
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
        PremiumHeroCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                if (hasDraft) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.history_save_draft_available),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                PremiumPrimaryButton(
                    text = stringResource(
                        if (hasDraft) {
                            R.string.history_save_button_resume
                        } else {
                            R.string.history_save_button
                        }
                    ),
                    onClick = onShowSheet,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Save
                )
            }
        }
    } else {
        PremiumHeroCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = ResultResolvedGreen.copy(alpha = 0.18f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                PremiumSecondaryButton(
                    text = stringResource(R.string.history_view_case),
                    onClick = { onOpenHistoryCase(savedCaseId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSaveSheet) {
        SaveDiagnosticBottomSheet(
            draft = draft,
            hasDraft = hasDraft,
            machineName = uiState.machineName ?: uiState.machineId,
            diagnosisTitle = titleText,
            diagnosisDescription = descriptionText,
            isSaving = uiState.isSavingCase,
            onHide = onHideSheet,
            onDraftChange = onDraftChange,
            onDiscard = onDiscardDraft,
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
    draft: SaveDiagnosticDraft,
    hasDraft: Boolean,
    machineName: String,
    diagnosisTitle: String,
    diagnosisDescription: String?,
    isSaving: Boolean,
    onHide: () -> Unit,
    onDraftChange: (SaveDiagnosticDraft) -> Unit,
    onDiscard: () -> Unit,
    onSave: (MaintenanceStatus, String, InitialMaintenanceAction, String) -> Unit
) {
    var showProblemNoteError by rememberSaveable { mutableStateOf(false) }
    var showDiscardDraftDialog by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val problemNoteBringIntoViewRequester = remember { BringIntoViewRequester() }
    val actionNoteBringIntoViewRequester = remember { BringIntoViewRequester() }
    val actionNoteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedInitialAction = InitialMaintenanceAction.valueOf(draft.initialAction)
    var isInitialActionMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var shouldFocusActionDetail by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedInitialAction, shouldFocusActionDetail) {
        if (shouldFocusActionDetail && selectedInitialAction != InitialMaintenanceAction.NONE) {
            actionNoteFocusRequester.requestFocus()
            actionNoteBringIntoViewRequester.bringIntoView()
            keyboardController?.show()
            shouldFocusActionDetail = false
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            if (!isSaving) {
                onHide()
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isInitialActionMenuExpanded) {
                            Modifier.blur(10.dp)
                        } else {
                            Modifier
                        }
                    ),
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

                    StatusSegmentedSelector(
                        selectedStatus = MaintenanceStatus.valueOf(draft.status),
                        onSelect = { status ->
                            onDraftChange(draft.copy(status = status.name))
                        }
                    )

                    OutlinedTextField(
                        value = draft.problemNote,
                        onValueChange = {
                            onDraftChange(draft.copy(problemNote = it))
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
                        expanded = isInitialActionMenuExpanded,
                        onExpandedChange = { isInitialActionMenuExpanded = it },
                        onSelect = {}
                    )

                    if (selectedInitialAction != InitialMaintenanceAction.NONE) {
                        OutlinedTextField(
                            value = draft.actionNote,
                            onValueChange = {
                                onDraftChange(draft.copy(actionNote = it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(actionNoteFocusRequester)
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
                        onClick = {
                            if (hasDraft) {
                                showDiscardDraftDialog = true
                            } else {
                                onDiscard()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) {
                        Text(stringResource(R.string.history_sheet_cancel))
                    }

                    Button(
                        onClick = {
                            val trimmedProblemNote = draft.problemNote.trim()
                            if (trimmedProblemNote.isBlank()) {
                                showProblemNoteError = true
                                return@Button
                            }
                            onSave(
                                MaintenanceStatus.valueOf(draft.status),
                                trimmedProblemNote,
                                selectedInitialAction,
                                draft.actionNote
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

            if (isInitialActionMenuExpanded) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f))
                )
                CenteredInitialActionPickerOverlay(
                    selectedAction = selectedInitialAction,
                    onDismiss = { isInitialActionMenuExpanded = false },
                    onSelect = { action ->
                        isInitialActionMenuExpanded = false
                        shouldFocusActionDetail = action != InitialMaintenanceAction.NONE
                        onDraftChange(
                            draft.copy(
                                initialAction = action.name,
                                actionNote = if (action == InitialMaintenanceAction.NONE) {
                                    ""
                                } else {
                                    draft.actionNote
                                }
                            )
                        )
                    }
                )
            }
        }
    }

    if (showDiscardDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDraftDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.history_sheet_discard_draft_title),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = { Text(stringResource(R.string.history_sheet_discard_draft_body)) },
            confirmButton = {
                FilledTonalButton(onClick = { showDiscardDraftDialog = false }) {
                    Text(stringResource(R.string.diagnostic_no))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDiscardDraftDialog = false
                        onDiscard()
                    }
                ) {
                    Text(stringResource(R.string.diagnostic_yes))
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun InitialActionSelector(
    selectedAction: InitialMaintenanceAction,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (InitialMaintenanceAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = stringResource(initialActionLabelRes(selectedAction)),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = initialActionIcon(selectedAction),
                    contentDescription = null
                )
            },
            maxLines = 1,
            textStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onExpandedChange(true) }
        )
    }
}

@Composable
private fun CenteredInitialActionPickerOverlay(
    selectedAction: InitialMaintenanceAction,
    onDismiss: () -> Unit,
    onSelect: (InitialMaintenanceAction) -> Unit
) {
    val options = listOf(
        InitialMaintenanceAction.NONE,
        InitialMaintenanceAction.TECHNICIAN_CONTACTED,
        InitialMaintenanceAction.COMPONENT_REPLACED,
        InitialMaintenanceAction.TEST_PERFORMED,
        InitialMaintenanceAction.OTHER
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
                options.forEachIndexed { index, action ->
                    val isSelected = selectedAction == action
                    Surface(
                        onClick = { onSelect(action) },
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
                            Color.Transparent
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
                                imageVector = initialActionIcon(action),
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(initialActionLabelRes(action)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = stringResource(initialActionDescriptionRes(action)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    if (index < options.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 2.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSegmentedSelector(
    selectedStatus: MaintenanceStatus,
    onSelect: (MaintenanceStatus) -> Unit
) {
    val options = listOf(
        MaintenanceStatus.PENDING,
        MaintenanceStatus.IN_PROGRESS,
        MaintenanceStatus.FINALIZED
    )
    val containerShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = containerShape
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = containerShape
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { status ->
            val selected = selectedStatus == status
            Surface(
                onClick = { onSelect(status) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                } else {
                    Color.Transparent
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(statusLabelRes(status)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SchematicsSection(
    schematics: List<SchematicDocument>,
    onOpenSchematic: (SchematicDocument) -> Unit
) {
    PremiumHeroCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        PremiumSectionEyebrow(text = stringResource(R.string.diagnostic_schematics_title))
        Text(
            text = stringResource(R.string.diagnostic_schematics_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        schematics.forEachIndexed { index, schematic ->
            PremiumSecondaryButton(
                text = schematic.title,
                onClick = { onOpenSchematic(schematic) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Filled.Description
            )

            if (index < schematics.lastIndex) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun endResultAccent(result: EndResult?): Color =
    when (result) {
        EndResult.RESOLVED -> ResultResolvedGreen.copy(alpha = 0.20f)
        EndResult.NO_ISSUE -> ResultWarningAmber.copy(alpha = 0.18f)
        EndResult.COMPONENT_FAULT -> ResultFaultRed.copy(alpha = 0.20f)
        null -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
    }

@Composable
private fun EndResultIcon(result: EndResult) {
    val (accentColor, iconRes) = when (result) {
        EndResult.RESOLVED -> ResultResolvedGreen to R.drawable.ic_result_resolved
        EndResult.NO_ISSUE -> ResultWarningAmber to R.drawable.ic_result_no_issue
        EndResult.COMPONENT_FAULT -> ResultFaultRed to R.drawable.ic_result_component_fault
    }
    val outerGlow = remember(accentColor) {
        Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.26f),
                accentColor.copy(alpha = 0.14f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = Modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .background(outerGlow, CircleShape)
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.20f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        ambientColor = accentColor.copy(alpha = 0.24f),
                        spotColor = accentColor.copy(alpha = 0.18f)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = accentColor.copy(alpha = 0.28f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.14f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = accentColor.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .background(
                        color = accentColor,
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun SuggestCard(text: String) {
    PremiumHeroCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    ) {
        PremiumSectionEyebrow(text = stringResource(R.string.diagnostic_suggestions_title))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun suggestedStatusFor(result: EndResult?): MaintenanceStatus =
    if (result == EndResult.RESOLVED) {
        MaintenanceStatus.FINALIZED
    } else {
        MaintenanceStatus.PENDING
    }

private fun statusLabelRes(status: MaintenanceStatus): Int =
    when (status) {
        MaintenanceStatus.PENDING -> R.string.history_status_pending
        MaintenanceStatus.IN_PROGRESS -> R.string.history_status_in_progress
        MaintenanceStatus.FINALIZED -> R.string.history_status_finalized
        MaintenanceStatus.CANCELED -> R.string.history_status_canceled
    }

private fun initialActionLabelRes(action: InitialMaintenanceAction): Int =
    when (action) {
        InitialMaintenanceAction.NONE -> R.string.history_initial_action_none
        InitialMaintenanceAction.TECHNICIAN_CONTACTED -> R.string.history_event_technician
        InitialMaintenanceAction.COMPONENT_REPLACED -> R.string.history_event_component
        InitialMaintenanceAction.TEST_PERFORMED -> R.string.history_event_test
        InitialMaintenanceAction.OTHER -> R.string.history_event_other
    }

private fun initialActionDescriptionRes(action: InitialMaintenanceAction): Int =
    when (action) {
        InitialMaintenanceAction.NONE -> R.string.history_initial_action_none_description
        InitialMaintenanceAction.TECHNICIAN_CONTACTED -> R.string.history_initial_action_technician_description
        InitialMaintenanceAction.COMPONENT_REPLACED -> R.string.history_initial_action_component_description
        InitialMaintenanceAction.TEST_PERFORMED -> R.string.history_initial_action_test_description
        InitialMaintenanceAction.OTHER -> R.string.history_initial_action_other_description
    }

private fun initialActionIcon(action: InitialMaintenanceAction): ImageVector =
    when (action) {
        InitialMaintenanceAction.NONE -> Icons.Filled.AccessTime
        InitialMaintenanceAction.TECHNICIAN_CONTACTED -> Icons.Filled.HeadsetMic
        InitialMaintenanceAction.COMPONENT_REPLACED -> Icons.Filled.Build
        InitialMaintenanceAction.TEST_PERFORMED -> Icons.Filled.Tune
        InitialMaintenanceAction.OTHER -> Icons.Filled.MoreHoriz
    }

private data class SaveDiagnosticDraft(
    val status: String,
    val problemNote: String = "",
    val initialAction: String = InitialMaintenanceAction.NONE.name,
    val actionNote: String = ""
) {
    fun hasContent(defaultStatus: String): Boolean =
        status != defaultStatus ||
            problemNote.isNotBlank() ||
            initialAction != InitialMaintenanceAction.NONE.name ||
            actionNote.isNotBlank()

    companion object {
        val Saver: Saver<SaveDiagnosticDraft, Any> = listSaver(
            save = { draft ->
                listOf(
                    draft.status,
                    draft.problemNote,
                    draft.initialAction,
                    draft.actionNote
                )
            },
            restore = { values ->
                SaveDiagnosticDraft(
                    status = values[0],
                    problemNote = values[1],
                    initialAction = values[2],
                    actionNote = values[3]
                )
            }
        )
    }
}
