package com.emagioda.myapp.presentation.screen.diagnostic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.NodeType
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenContacts: () -> Unit
) {
    val context = LocalContext.current
    val vm: DiagnosticViewModel = viewModel(
        factory = DiagnosticViewModel.Factory(
            getTree = ServiceLocator.provideGetTreeUseCase(context),
            machineId = machineId
        )
    )
    val state = vm.uiState
    val node = state.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Diagnóstico") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (node == null) {
                Text("Error al cargar el flujo.")
                return@Box
            }

            // Centramos todo el contenido
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                when (node.type) {
                    // -------------------- QUESTION --------------------
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
                            Button(onClick = { vm.answerYes() }) { Text("Sì") }
                            OutlinedButton(onClick = { vm.answerNo() }) { Text("No") }
                        }
                    }

                    // -------------------- ACTION ----------------------
                    NodeType.ACTION -> {
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
                        node.action?.steps?.takeIf { it.isNotEmpty() }?.let { steps ->
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                steps.forEach { step ->
                                    Text("• $step", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        node.action?.safetyNotes?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Seguridad: $it",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.continueAction() }) { Text("Continua") }
                    }

                    // ---------------------- END -----------------------
                    NodeType.END -> {
                        // 👇 Solo mostramos una vez: título (grande) y, si existe, descripción (chica)
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

                        Spacer(Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    vm.restart()
                                    onRestartToHome()
                                }
                            ) { Text("Riavvia") }

                            OutlinedButton(onClick = onOpenContacts) {
                                Text("¿Necesitás ayuda? Ver contactos")
                            }
                        }
                    }
                }
            }
        }
    }
}
