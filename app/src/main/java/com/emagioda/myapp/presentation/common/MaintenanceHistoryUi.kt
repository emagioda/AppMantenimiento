package com.emagioda.myapp.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val tone = statusTone(status)
    Surface(
        modifier = modifier,
        color = tone.container,
        contentColor = tone.content,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, tone.content.copy(alpha = 0.22f))
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MaintenanceResultChip(
    result: EndResult,
    modifier: Modifier = Modifier
) {
    val tone = resultTone(result)
    Surface(
        modifier = modifier,
        color = tone.container,
        contentColor = tone.content,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, tone.content.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = tone.iconRes),
                contentDescription = null,
                tint = tone.content,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(result.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MaintenanceEventIcon(
    type: MaintenanceEventType,
    modifier: Modifier = Modifier
) {
    val (icon, color) = type.iconAndColor()
    Surface(
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun formatHistoryDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.ITALIAN)
    return formatter.format(Date(timestamp))
}

private data class HistoryTone(
    val container: Color,
    val content: Color
)

private data class HistoryResultTone(
    val container: Color,
    val content: Color,
    val iconRes: Int
)

private fun statusTone(status: MaintenanceStatus): HistoryTone =
    when (status) {
        MaintenanceStatus.PENDING -> HistoryTone(
            container = HistoryPendingAmber.copy(alpha = 0.18f),
            content = HistoryPendingAmber
        )
        MaintenanceStatus.IN_PROGRESS -> HistoryTone(
            container = HistoryInProgressBlue.copy(alpha = 0.18f),
            content = HistoryInProgressBlue
        )
        MaintenanceStatus.FINALIZED -> HistoryTone(
            container = HistoryFinalizedGreen.copy(alpha = 0.18f),
            content = HistoryFinalizedGreen
        )
        MaintenanceStatus.CANCELED -> HistoryTone(
            container = Color(0x33E57373),
            content = Color(0xFFE57373)
        )
    }

private fun resultTone(result: EndResult): HistoryResultTone =
    when (result) {
        EndResult.RESOLVED -> HistoryResultTone(
            container = HistoryFinalizedGreen.copy(alpha = 0.18f),
            content = HistoryFinalizedGreen,
            iconRes = R.drawable.ic_result_resolved
        )
        EndResult.NO_ISSUE -> HistoryResultTone(
            container = HistoryPendingAmber.copy(alpha = 0.18f),
            content = HistoryPendingAmber,
            iconRes = R.drawable.ic_result_no_issue
        )
        EndResult.COMPONENT_FAULT -> HistoryResultTone(
            container = Color(0x33E57373),
            content = Color(0xFFE57373),
            iconRes = R.drawable.ic_result_component_fault
        )
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
        MaintenanceEventType.OTHER -> Icons.Default.Flag to Color(0xFF96A9FF)
        MaintenanceEventType.RESOLUTION -> Icons.Default.Check to HistoryFinalizedGreen
        MaintenanceEventType.CASE_UPDATED -> Icons.Default.EditNote to HistoryInProgressBlue
        MaintenanceEventType.CASE_REOPENED -> Icons.Default.RestartAlt to HistoryPendingAmber
        MaintenanceEventType.CASE_CANCELED -> Icons.Default.DeleteOutline to Color(0xFFE57373)
    }
