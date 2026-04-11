package com.emagioda.myapp.presentation.screen.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
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
import com.emagioda.myapp.presentation.common.PremiumEmptyState
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumSectionEyebrow
import com.emagioda.myapp.presentation.common.formatHistoryDateTime
import com.emagioda.myapp.presentation.common.resolveDisplayText
import com.emagioda.myapp.presentation.viewmodel.HistoryUiState
import com.emagioda.myapp.ui.theme.HistoryFinalizedGreen
import com.emagioda.myapp.ui.theme.HistoryInProgressBlue
import com.emagioda.myapp.ui.theme.HistoryPendingAmber

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
        modifier = modifier.fillMaxSize()
    ) {
        PremiumHeroCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            contentPadding = PaddingValues(18.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
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
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilterChip(
                        label = stringResource(R.string.history_status_pending),
                        selected = uiState.selectedStatus == MaintenanceStatus.PENDING,
                        onClick = { onStatusToggle(MaintenanceStatus.PENDING) }
                    )
                    HistoryFilterChip(
                        label = stringResource(R.string.history_status_in_progress),
                        selected = uiState.selectedStatus == MaintenanceStatus.IN_PROGRESS,
                        onClick = { onStatusToggle(MaintenanceStatus.IN_PROGRESS) }
                    )
                    HistoryFilterChip(
                        label = stringResource(R.string.history_status_finalized),
                        selected = uiState.selectedStatus == MaintenanceStatus.FINALIZED,
                        onClick = { onStatusToggle(MaintenanceStatus.FINALIZED) }
                    )
                }
            }
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
                            title = stringResource(section.status.titleRes()),
                            count = section.items.size
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
    PremiumEmptyState(
        title = title,
        subtitle = subtitle,
        icon = Icons.Default.Search,
        overline = stringResource(R.string.history_overline)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    )
}

@Composable
private fun HistorySectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PremiumSectionEyebrow(text = count.toString())
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
    val accentColor = historyStatusAccent(item.status)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 0.dp, top = 16.dp, bottom = 16.dp)
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(999.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showMachineIdentity) {
                            PremiumSectionEyebrow(text = item.machineNameSnapshot)
                        }
                        Text(
                            text = problemTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    MaintenanceStatusChip(status = item.status)
                }

                MaintenanceResultChip(result = item.endResult)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryMetaLine(
                        label = stringResource(R.string.history_detected_at),
                        value = formatHistoryDateTimeWithHoursSuffix(item.openedAt),
                        inline = true
                    )
                    HistoryMetaLine(
                        label = stringResource(R.string.history_last_activity_at),
                        value = formatHistoryDateTimeWithHoursSuffix(lastActivityAt),
                        inline = true
                    )
                    latestAction?.let {
                        HistoryMetaLine(
                            label = stringResource(R.string.history_last_action),
                            value = it,
                            inline = true,
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMetaLine(
    label: String,
    value: String,
    inline: Boolean = false,
    singleLine: Boolean = false
) {
    if (inline) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
        )
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip
            )
        }
    }
}

private fun formatHistoryDateTimeWithHoursSuffix(timestamp: Long): String =
    "${formatHistoryDateTime(timestamp)} hs"

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

private fun historyStatusAccent(status: MaintenanceStatus): Color =
    when (status) {
        MaintenanceStatus.PENDING -> HistoryPendingAmber
        MaintenanceStatus.IN_PROGRESS -> HistoryInProgressBlue
        MaintenanceStatus.FINALIZED -> HistoryFinalizedGreen
        MaintenanceStatus.CANCELED -> Color(0xFFE57373)
    }
