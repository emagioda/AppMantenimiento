package com.emagioda.myapp.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PremiumScreenBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor,
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )
        content()
    }
}

@Composable
fun PremiumSectionEyebrow(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.74f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PremiumHeroCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
    contentPadding: PaddingValues = PaddingValues(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.24f),
                spotColor = accentColor.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor,
                            Color.Transparent
                        ),
                        radius = 560f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        if (leadingIcon != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PremiumSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        if (leadingIcon != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PremiumActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PremiumEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    overline: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PremiumHeroCard(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(horizontal = 24.dp),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
        ) {
            overline?.let {
                PremiumSectionEyebrow(text = it)
            }
            icon?.let {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun InitialsBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
