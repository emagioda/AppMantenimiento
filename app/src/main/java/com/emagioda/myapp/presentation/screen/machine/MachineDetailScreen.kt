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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
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

            machine == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            machine.imageName?.let { imageName ->
                                val resId = resolveDrawableResId(imageName)

                                if (resId != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = machine.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(20.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                }
                            } ?: Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            machine.description?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (uiState.historyOverview.hasOpenCases) {
                                OpenCasesAlert(
                                    openCasesCount = uiState.historyOverview.openCasesCount,
                                    onClick = { onOpenHistory(machineId) }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { onStartDiagnostic(machineId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text(
                                text = stringResource(R.string.machine_detail_start).uppercase(),
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HistorySwipeZone(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(28.dp)
                            .zIndex(2f),
                        onOpenHistory = { onOpenHistory(machineId) }
                    )

                    HistoryEdgeHandle(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(vertical = 32.dp)
                            .zIndex(3f),
                        hasOpenCases = uiState.historyOverview.hasOpenCases,
                        onClick = { onOpenHistory(machineId) }
                    )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.machine_detail_open_cases_alert_title,
                        openCasesCount,
                        openCasesCount
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.machine_detail_open_cases_alert_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
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
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
        color = containerColor,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .width(34.dp)
                .height(92.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 3.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.machine_detail_history_handle_cd),
                modifier = Modifier.size(30.dp),
                tint = Color.White
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
