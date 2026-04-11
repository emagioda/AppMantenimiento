package com.emagioda.myapp.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumPrimaryButton
import com.emagioda.myapp.presentation.common.PremiumScreenBackground
import com.emagioda.myapp.presentation.common.PremiumSectionEyebrow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title)) }
            )
        }
    ) { inner ->
        PremiumScreenBackground(
            modifier = Modifier.padding(inner),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(0.75f))

                    Image(
                        painter = painterResource(id = R.drawable.splash_logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .widthIn(max = 220.dp)
                    )

                    Spacer(Modifier.weight(0.75f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PremiumHeroCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
                        ) {
                            PremiumSectionEyebrow(
                                text = stringResource(R.string.home_overline),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = stringResource(R.string.home_header),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PremiumPrimaryButton(
                            text = stringResource(R.string.home_scan_button).uppercase(),
                            onClick = onNavigateToScanner,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Default.QrCodeScanner
                        )
                    }

                    Spacer(Modifier.weight(1f))
                }

                HomeSecondaryHistoryButton(
                    title = stringResource(R.string.home_history_button),
                    subtitle = stringResource(R.string.home_history_support),
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HomeSecondaryHistoryButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
