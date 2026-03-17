package com.emagioda.myapp.presentation.screen.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.presentation.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineHistoryScreen(
    machineId: String,
    onBack: () -> Unit,
    onOpenCase: (Long) -> Unit
) {
    val context = LocalContext.current
    val vm: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(
            machineIdFilter = machineId,
            getMachineDetail = ServiceLocator.provideGetMachineDetail(context),
            observeMaintenanceCases = ServiceLocator.provideObserveMaintenanceCases(context),
            prioritizeUnresolved = true
        )
    )
    val uiState = vm.uiState
    val machineName = uiState.machineNameFilter ?: machineId
    val handleBack = {
        if (uiState.showCanceledOnly) {
            vm.onCanceledToggle()
        } else {
            onBack()
        }
    }
    val canceledIconTint = if (uiState.showCanceledOnly) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val emptyTitleRes = if (uiState.showCanceledOnly) {
        R.string.machine_history_canceled_empty_title
    } else {
        R.string.machine_history_empty_title
    }
    val emptySubtitleRes = if (uiState.showCanceledOnly) {
        R.string.machine_history_canceled_empty_subtitle
    } else {
        R.string.machine_history_empty_subtitle
    }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    BackHandler(enabled = uiState.showCanceledOnly) {
        vm.onCanceledToggle()
    }

    AnimatedVisibility(
        visible = contentVisible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth / 3 },
            animationSpec = tween(durationMillis = 280)
        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = ExitTransition.None
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.machine_history_fallback_title)) },
                    navigationIcon = {
                        IconButton(onClick = handleBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = vm::onCanceledToggle) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = stringResource(
                                    if (uiState.showCanceledOnly) {
                                        R.string.history_show_active_cases_cd
                                    } else {
                                        R.string.history_show_canceled_cases_cd
                                    }
                                ),
                                tint = canceledIconTint
                            )
                        }
                    }
                )
            }
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

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Text(
                            text = machineName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        HistoryContent(
                            uiState = uiState,
                            emptyTitle = stringResource(emptyTitleRes),
                            emptySubtitle = stringResource(emptySubtitleRes),
                            searchPlaceholder = stringResource(R.string.history_search_placeholder),
                            onSearchQueryChange = vm::onSearchQueryChange,
                            onStatusToggle = vm::onStatusToggle,
                            onOpenCase = onOpenCase,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
