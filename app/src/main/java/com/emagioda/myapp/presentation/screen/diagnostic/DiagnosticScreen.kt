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
import com.emagioda.myapp.domain.model.QuestionMode
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

                    //----------------------------------------------------------------------
                    //   PREGUNTAS
                    //----------------------------------------------------------------------
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

                        when (node.mode) {

                            QuestionMode.CONTINUE_ONLY -> {
                                Button(onClick = vm::answerYes) {
                                    Text("Continua")
                                }
                            }

                            QuestionMode.YES_NO -> {
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
                        }
                    }

                    //----------------------------------------------------------------------
                    //   END
                    //----------------------------------------------------------------------
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

                        // Repuestos sugeridos
                        node.parts?.takeIf { it.isNotEmpty() }?.let { parts ->
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.diagnostic_parts_needed),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                parts.forEach { p ->
                                    OutlinedCard {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("ID: ${p.id}", style = MaterialTheme.typography.bodyLarge)
                                            p.qty?.let { q ->
                                                Spacer(Modifier.height(4.dp))
                                                Text(stringResource(R.string.diagnostic_part_qty, q))
                                            }
                                            p.note?.let { n ->
                                                Spacer(Modifier.height(4.dp))
                                                Text(n, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

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
