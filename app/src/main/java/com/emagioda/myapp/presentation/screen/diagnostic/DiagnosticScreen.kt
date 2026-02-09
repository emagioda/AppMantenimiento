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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.presentation.common.SafetyWarningDialog
import com.emagioda.myapp.presentation.screen.diagnostic.components.DiagnosticPartsSection
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenTechnicians: () -> Unit = onOpenContacts,
    onOpenProviders: () -> Unit = onOpenContacts
) {
    val vm: DiagnosticViewModel = viewModel(
        factory = DiagnosticViewModel.Factory(
            getTree = ServiceLocator.provideGetTreeUseCase(LocalContext.current),
            machineId = machineId
        )
    )
    val uiState = vm.uiState
    val node = uiState.current

    val showSafetyDialog = (node?.safetyWarning == true) && !uiState.safetyWarningDismissed

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
                        NodeType.QUESTION -> QuestionContent(node = node, vm = vm)
                        NodeType.END -> EndContent(
                            node = node,
                            vm = vm,
                            onRestartToHome = onRestartToHome,
                            onOpenTechnicians = onOpenTechnicians
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
    vm: DiagnosticViewModel
) {
    AnimatedContent(
        targetState = node,
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(300))).togetherWith(
                slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(300)))
        },
        label = "question-transition"
    ) { targetNode ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- ZONA SUPERIOR: LA TARJETA CON LA PREGUNTA ---
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
                            text = targetNode.title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        targetNode.description?.let {
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

            // --- ZONA INFERIOR: LOS BOTONES GRANDES ---
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
                            // Botón NO
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

                            // Botón SÍ
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
    onOpenTechnicians: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            // Aumentamos el padding superior e inferior para darle aire
            .padding(top = 32.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EndResultIcon(node.result ?: EndResult.NO_ISSUE)

        Spacer(Modifier.height(24.dp))

        Text(
            text = node.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        node.description?.let {
            Spacer(Modifier.height(24.dp))
            SuggestCard(it)
        }

        node.parts?.takeIf { it.isNotEmpty() }?.let { parts ->
            Spacer(Modifier.height(24.dp))
            DiagnosticPartsSection(parts)
        }

        Spacer(Modifier.height(40.dp))

        // --- BOTONES UNIFICADOS Y VISIBLES ---

        // 1. Botón Técnicos: CAMBIADO a FilledTonalButton para mejor visibilidad
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

        // 2. Botón Home (Acción Principal)
        Button(
            onClick = {
                vm.restart()
                onRestartToHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.diagnostic_home).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EndResultIcon(result: EndResult) {
    val (bg, icon) = when (result) {
        EndResult.RESOLVED -> Color(0xFF4CAF50) to Icons.Filled.Check
        EndResult.NO_ISSUE -> Color(0xFFFFC107) to Icons.Filled.Warning
        EndResult.COMPONENT_FAULT -> Color(0xFFE53935) to Icons.Filled.Build
    }

    Box(
        modifier = Modifier.size(120.dp), // Icono final un poco más grande
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
    // Usamos el mismo estilo de tarjeta industrial para las sugerencias
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