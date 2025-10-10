package com.emagioda.myapp.presentation.screen.diagnostic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenTechnicians: () -> Unit = onOpenContacts,
    onOpenProviders: () -> Unit = onOpenContacts
) {
    val vm: DiagnosticViewModel = viewModel(
        factory = DiagnosticViewModel.Factory(
            getTree = ServiceLocator.provideGetTreeUseCase(LocalContext.current),
            machineId = machineId
        )
    )
    val node = vm.uiState.current

    BackHandler(enabled = vm.canGoBack()) { vm.goBack() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.diagnostic_title)) },
                navigationIcon = {
                    if (vm.canGoBack()) {
                        IconButton(onClick = { vm.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (node == null) {
                Text(stringResource(R.string.diagnostic_error_loading))
                return@Box
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (node.type) {
                    NodeType.QUESTION -> {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        node.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = vm::answerYes) {
                                Text(stringResource(R.string.diagnostic_yes))
                            }
                            OutlinedButton(
                                onClick = vm::answerNo,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.diagnostic_no))
                            }
                        }
                    }

                    NodeType.END -> {
                        Text(
                            text = node.title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        node.description?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        if (node.providersShortcut == true) {
                            OutlinedButton(
                                onClick = onOpenProviders,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.contacts_providers_shortcut))
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        Text(
                            text = stringResource(R.string.diagnostic_help),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = onOpenTechnicians,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(stringResource(R.string.contacts_tech_shortcut))
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            vm.restart()
                            onRestartToHome()
                        }) {
                            Text(stringResource(R.string.diagnostic_home))
                        }
                    }
                }
            }
        }
    }
}
