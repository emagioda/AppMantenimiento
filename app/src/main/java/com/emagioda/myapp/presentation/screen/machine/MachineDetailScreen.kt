package com.emagioda.myapp.presentation.screen.machine

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumPrimaryButton
import com.emagioda.myapp.presentation.common.PremiumScreenBackground
import com.emagioda.myapp.presentation.common.resolveDrawableResId
import com.emagioda.myapp.presentation.viewmodel.MachineDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    machineId: String,
    onBack: () -> Unit,
    onStartDiagnostic: (String) -> Unit,
    onOpenHistory: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: MachineDetailViewModel = viewModel(
        factory = MachineDetailViewModel.Factory(
            getMachineDetail = ServiceLocator.provideGetMachineDetail(context),
            observeMaintenanceCases = ServiceLocator.provideObserveMaintenanceCases(context),
            machineId = machineId
        )
    )
    val uiState = vm.uiState
    val machine = uiState.machine

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = machine?.name ?: stringResource(R.string.machine_detail_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        PremiumScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                machine == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                uiState.errorResId ?: R.string.machine_detail_error_loading
                            )
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    shape = RoundedCornerShape(28.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val resId = machine.imageName?.let(::resolveDrawableResId)
                                        if (resId != null) {
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = machine.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 6.dp, vertical = 8.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PrecisionManufacturing,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Text(
                                                    text = stringResource(R.string.machine_detail_no_image),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                machine.description?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (uiState.historyOverview.hasOpenCases) {
                                OpenCasesAlert(
                                    openCasesCount = uiState.historyOverview.openCasesCount,
                                    onClick = { onOpenHistory(machineId) }
                                )
                            }

                            PremiumPrimaryButton(
                                text = stringResource(R.string.machine_detail_start).uppercase(),
                                onClick = { onStartDiagnostic(machineId) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = Icons.Default.PrecisionManufacturing
                            )
                        }

                        HistorySwipeZone(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(32.dp),
                            onOpenHistory = { onOpenHistory(machineId) }
                        )

                        HistoryEdgeHandle(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(vertical = 32.dp),
                            hasOpenCases = uiState.historyOverview.hasOpenCases,
                            onClick = { onOpenHistory(machineId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenCasesAlert(
    openCasesCount: Int,
    onClick: () -> Unit
) {
    PremiumHeroCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.76f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.machine_detail_open_cases_alert_title,
                        openCasesCount,
                        openCasesCount
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.machine_detail_open_cases_alert_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.machine_detail_open_cases_cta),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HistoryEdgeHandle(
    modifier: Modifier = Modifier,
    hasOpenCases: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (hasOpenCases) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
        color = containerColor,
        shadowElevation = 8.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .width(38.dp)
                .height(106.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.machine_detail_history_handle_cd),
                modifier = Modifier.size(32.dp),
                tint = if (hasOpenCases) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun HistorySwipeZone(
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit
) {
    Box(
        modifier = modifier.pointerInput(onOpenHistory) {
            var totalDrag = 0f
            var hasTriggered = false
            val threshold = 72.dp.toPx()

            detectHorizontalDragGestures(
                onDragStart = {
                    totalDrag = 0f
                    hasTriggered = false
                },
                onHorizontalDrag = { _, dragAmount ->
                    totalDrag += dragAmount
                    if (!hasTriggered && totalDrag < -threshold) {
                        hasTriggered = true
                        onOpenHistory()
                    }
                },
                onDragEnd = {
                    totalDrag = 0f
                    hasTriggered = false
                },
                onDragCancel = {
                    totalDrag = 0f
                    hasTriggered = false
                }
            )
        }
    )
}
