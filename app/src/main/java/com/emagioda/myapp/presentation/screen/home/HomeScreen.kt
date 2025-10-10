package com.emagioda.myapp.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emagioda.myapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.home_settings_cd))
                    }
                }
            )
        }
    ) { inner ->
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeHeader()

                Spacer(Modifier.height(32.dp))

                PrimaryScanButton(onClick = onNavigateToScanner)

                Spacer(Modifier.height(36.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

                Spacer(Modifier.height(28.dp))

                HelpSection(onContactsClick = onNavigateToContacts)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Text(
        text = stringResource(R.string.home_header),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PrimaryScanButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(stringResource(R.string.home_scan_button), fontSize = 18.sp)
    }
}

@Composable
private fun HelpSection(onContactsClick: () -> Unit) {
    Text(
        text = stringResource(R.string.home_help_title),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp
    )
    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onContactsClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Text(stringResource(R.string.home_help_contacts), fontSize = 17.sp)
    }
}
