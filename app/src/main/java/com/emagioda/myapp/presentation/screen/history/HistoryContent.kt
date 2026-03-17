package com.emagioda.myapp.presentation.screen.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.MaintenanceCaseSummary
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.presentation.common.MaintenanceResultChip
import com.emagioda.myapp.presentation.common.MaintenanceStatusChip
import com.emagioda.myapp.presentation.common.formatHistoryDateTime
import com.emagioda.myapp.presentation.common.resolveDisplayText
import com.emagioda.myapp.presentation.viewmodel.HistoryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryContent(
    uiState: HistoryUiState,
    emptyTitle: String,
    emptySubtitle: String,
    searchPlaceholder: String,
    onSearchQueryChange: (String) -> Unit,
    onStatusToggle: (MaintenanceStatus) -> Unit,
    onOpenCase: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = maintenanceStatusSections(uiState.cases)
    val showMachineIdentity = uiState.machineIdFilter.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            placeholder = {
                Text(searchPlaceholder)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        )

        if (!uiState.showCanceledOnly) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatus == MaintenanceStatus.PENDING,
                        onClick = { onStatusToggle(MaintenanceStatus.PENDING) },
                        label = { Text(stringResource(R.string.history_status_pending)) }
                    )
                    FilterChip(
                        selected = uiState.selectedStatus == MaintenanceStatus.IN_PROGRESS,
                        onClick = { onStatusToggle(MaintenanceStatus.IN_PROGRESS) },
                        label = { Text(stringResource(R.string.history_status_in_progress)) }
                    )
                    FilterChip(
                        selected = uiState.selectedStatus == MaintenanceStatus.FINALIZED,
                        onClick = { onStatusToggle(MaintenanceStatus.FINALIZED) },
                        label = { Text(stringResource(R.string.history_status_finalized)) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        if (uiState.cases.isEmpty()) {
            EmptyHistoryState(
                title = emptyTitle,
                subtitle = emptySubtitle
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sections.forEach { section ->
                    item(key = "header_${section.status.name}") {
                        HistorySectionHeader(
                            title = stringResource(section.status.titleRes())
                        )
                    }

                    items(
                        items = section.items,
                        key = { item -> item.id }
                    ) { item ->
                        HistoryCaseCard(
                            item = item,
                            showMachineIdentity = showMachineIdentity,
                            onClick = { onOpenCase(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EmptyHistoryState(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistorySectionHeader(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun HistoryCaseCard(
    item: MaintenanceCaseSummary,
    showMachineIdentity: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val problemTitle = resolveDisplayText(
        context,
        item.problemSummary?.takeIf { it.isNotBlank() } ?: item.diagnosisTitle
    )
    val latestAction = item.latestEventTitle
        ?.let { resolveDisplayText(context, it) }
        ?.takeIf { action ->
            action.isNotBlank() &&
                action != problemTitle &&
                action != context.getString(R.string.history_event_problem)
        }
    val lastActivityAt = item.latestEventAt ?: item.updatedAt

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showMachineIdentity) {
                        Text(
                            text = item.machineNameSnapshot,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = problemTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                MaintenanceStatusChip(status = item.status)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaintenanceResultChip(result = item.endResult)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.history_detected_at)}: ${formatHistoryDateTime(item.openedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource(R.string.history_last_activity_at)}: ${formatHistoryDateTime(lastActivityAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                latestAction?.let {
                    Text(
                        text = "${stringResource(R.string.history_last_action)}: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class HistoryStatusSection(
    val status: MaintenanceStatus,
    val items: List<MaintenanceCaseSummary>
)

private fun maintenanceStatusSections(
    cases: List<MaintenanceCaseSummary>
): List<HistoryStatusSection> =
    listOf(
        MaintenanceStatus.PENDING,
        MaintenanceStatus.IN_PROGRESS,
        MaintenanceStatus.FINALIZED,
        MaintenanceStatus.CANCELED
    ).mapNotNull { status ->
        val sectionItems = cases.filter { it.status == status }
        if (sectionItems.isEmpty()) {
            null
        } else {
            HistoryStatusSection(
                status = status,
                items = sectionItems
            )
        }
    }

private fun MaintenanceStatus.titleRes(): Int =
    when (this) {
        MaintenanceStatus.PENDING -> R.string.history_status_pending
        MaintenanceStatus.IN_PROGRESS -> R.string.history_status_in_progress
        MaintenanceStatus.FINALIZED -> R.string.history_status_finalized
        MaintenanceStatus.CANCELED -> R.string.history_status_canceled
    }
