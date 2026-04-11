package com.emagioda.myapp.presentation.screen.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.presentation.common.PremiumEmptyState
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumScreenBackground
import com.emagioda.myapp.presentation.common.PremiumSectionEyebrow
import com.emagioda.myapp.presentation.common.resolveDrawableResId
import com.emagioda.myapp.presentation.viewmodel.HistoryMachineListItemUiState
import com.emagioda.myapp.presentation.viewmodel.HistoryMachinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenMachine: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: HistoryMachinesViewModel = viewModel(
        factory = HistoryMachinesViewModel.Factory(
            getMachineIds = ServiceLocator.provideGetMachineIds(context),
            getMachineDetail = ServiceLocator.provideGetMachineDetail(context)
        )
    )
    val uiState = vm.uiState

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
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
            modifier = Modifier.padding(innerPadding),
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

                uiState.machines.isEmpty() -> {
                    PremiumEmptyState(
                        title = stringResource(R.string.history_machines_empty_title),
                        subtitle = stringResource(R.string.history_machines_empty_subtitle),
                        icon = Icons.Default.PrecisionManufacturing,
                        overline = stringResource(R.string.history_overline)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            PremiumHeroCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = stringResource(R.string.history_machine_selector_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.history_machine_selector_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = uiState.machines,
                            key = { machine -> machine.id }
                        ) { machine ->
                            HistoryMachineCard(
                                item = machine,
                                onClick = { onOpenMachine(machine.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMachineCard(
    item: HistoryMachineListItemUiState,
    onClick: () -> Unit
) {
    val imageResId = item.imageName
        ?.takeIf { it.isNotBlank() }
        ?.let(::resolveDrawableResId)

    PremiumHeroCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(96.dp)
                    .height(124.dp),
                shape = RoundedCornerShape(22.dp),
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
                    if (imageResId != null) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumSectionEyebrow(text = item.id)
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.history_view_machine),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
