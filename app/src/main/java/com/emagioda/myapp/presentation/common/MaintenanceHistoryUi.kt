package com.emagioda.myapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.MaintenanceEventType
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.ui.theme.HistoryFinalizedGreen
import com.emagioda.myapp.ui.theme.HistoryInProgressBlue
import com.emagioda.myapp.ui.theme.HistoryPendingAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceStatusChip(
    status: MaintenanceStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (status) {
        MaintenanceStatus.PENDING -> HistoryPendingAmber.copy(alpha = 0.18f) to HistoryPendingAmber
        MaintenanceStatus.IN_PROGRESS -> HistoryInProgressBlue.copy(alpha = 0.18f) to HistoryInProgressBlue
        MaintenanceStatus.FINALIZED -> HistoryFinalizedGreen.copy(alpha = 0.18f) to HistoryFinalizedGreen
        MaintenanceStatus.CANCELED -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f) to
            MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MaintenanceResultChip(
    result: EndResult,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (result) {
        EndResult.RESOLVED -> HistoryFinalizedGreen.copy(alpha = 0.18f) to HistoryFinalizedGreen
        EndResult.NO_ISSUE -> HistoryPendingAmber.copy(alpha = 0.18f) to HistoryPendingAmber
        EndResult.COMPONENT_FAULT -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f) to
            MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = stringResource(result.labelRes()),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MaintenanceEventIcon(
    type: MaintenanceEventType,
    modifier: Modifier = Modifier
) {
    val (icon, color) = type.iconAndColor()
    Box(
        modifier = modifier
            .size(38.dp)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

fun formatHistoryDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun MaintenanceStatus.labelRes(): Int =
    when (this) {
        MaintenanceStatus.PENDING -> R.string.history_status_pending
        MaintenanceStatus.IN_PROGRESS -> R.string.history_status_in_progress
        MaintenanceStatus.FINALIZED -> R.string.history_status_finalized
        MaintenanceStatus.CANCELED -> R.string.history_status_canceled
    }

private fun EndResult.labelRes(): Int =
    when (this) {
        EndResult.RESOLVED -> R.string.history_result_resolved
        EndResult.NO_ISSUE -> R.string.history_result_no_issue
        EndResult.COMPONENT_FAULT -> R.string.history_result_component_fault
    }

private fun MaintenanceEventType.iconAndColor(): Pair<ImageVector, Color> =
    when (this) {
        MaintenanceEventType.PROBLEM_DETECTED -> Icons.Default.Search to HistoryPendingAmber
        MaintenanceEventType.TECHNICIAN_CONTACTED -> Icons.Default.Engineering to HistoryInProgressBlue
        MaintenanceEventType.COMPONENT_REPLACED -> Icons.Default.Build to HistoryInProgressBlue
        MaintenanceEventType.TEST_PERFORMED -> Icons.Default.Construction to HistoryInProgressBlue
        MaintenanceEventType.OBSERVATION -> Icons.Default.Info to Color(0xFF9FB3C8)
        MaintenanceEventType.OTHER -> Icons.Default.Flag to Color(0xFFB7A1E5)
        MaintenanceEventType.RESOLUTION -> Icons.Default.Check to HistoryFinalizedGreen
        MaintenanceEventType.CASE_UPDATED -> Icons.Default.EditNote to HistoryInProgressBlue
        MaintenanceEventType.CASE_REOPENED -> Icons.Default.RestartAlt to HistoryPendingAmber
        MaintenanceEventType.CASE_CANCELED -> Icons.Default.DeleteOutline to Color(0xFFE57373)
    }
