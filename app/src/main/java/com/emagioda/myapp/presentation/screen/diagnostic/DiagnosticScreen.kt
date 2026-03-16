package com.emagioda.myapp.presentation.screen.diagnostic

import android.content.Context
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.DiagnosticNode
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.domain.model.QuestionMode
import com.emagioda.myapp.domain.model.SchematicDocument
import com.emagioda.myapp.presentation.common.SafetyWarningDialog
import com.emagioda.myapp.presentation.common.SchematicPdfOpener
import com.emagioda.myapp.presentation.screen.diagnostic.components.DiagnosticPartsSection
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel
import com.emagioda.myapp.ui.theme.ResultFaultRed
import com.emagioda.myapp.ui.theme.ResultResolvedGreen
import com.emagioda.myapp.ui.theme.ResultWarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenTechnicians: () -> Unit = onOpenContacts,
    onOpenProviders: () -> Unit = onOpenContacts,
    onOpenFilteredContacts: (String, String) -> Unit = { _, _ -> }
) {
    val vm: DiagnosticViewModel = viewModel(
        factory = DiagnosticViewModel.Factory(
            getTree = ServiceLocator.provideGetTreeUseCase(LocalContext.current),
            machineId = machineId
        )
    )
    val uiState = vm.uiState
    val node = uiState.current
    val context = LocalContext.current
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
                            vm = vm,
                            onRestartToHome = onRestartToHome,
                            onOpenTechnicians = onOpenTechnicians,
                            onOpenFilteredContacts = onOpenFilteredContacts,
                            onOpenSchematic = ::openSchematic
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
private fun EndContent(
    node: DiagnosticNode,
    vm: DiagnosticViewModel,
    onRestartToHome: () -> Unit,
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

private fun resolveDisplayText(context: Context, rawText: String): String {
    val resId = context.resources.getIdentifier(rawText, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else rawText
}
