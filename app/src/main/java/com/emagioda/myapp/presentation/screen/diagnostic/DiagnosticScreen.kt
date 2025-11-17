package com.emagioda.myapp.presentation.screen.diagnostic

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    machineId: String,
    onRestartToHome: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenTechnicians: () -> Unit = onOpenContacts,
    onOpenProviders: () -> Unit = onOpenContacts   // reservado para futuro
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
            contentAlignment = Alignment.Center        // <- centrado vertical
        ) {
            if (node == null) {
                Text(stringResource(R.string.diagnostic_error_loading))
                return@Box
            }

            when (node.type) {
                NodeType.QUESTION -> QuestionContent(node = node, vm = vm)
                NodeType.END -> EndContent(
                    node = node,
                    vm = vm,
                    onRestartToHome = onRestartToHome,
                    onOpenTechnicians = onOpenTechnicians
                )
            }
        }
    }
}

@Composable
private fun QuestionContent(
    node: DiagnosticNode,
    vm: DiagnosticViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.92f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
}

@Composable
private fun EndContent(
    node: DiagnosticNode,
    vm: DiagnosticViewModel,
    onRestartToHome: () -> Unit,
    onOpenTechnicians: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.92f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // icono
        EndResultIcon(node.result ?: EndResult.NO_ISSUE)

        Spacer(Modifier.height(24.dp))

        // título principal
        Text(
            text = node.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        // Card de sugerencias (aparecerá cuando uses description)
        node.description?.let {
            Spacer(Modifier.height(20.dp))
            SuggestCard(it)
        }

        // Card de recambios si hay partes
        node.parts?.takeIf { it.isNotEmpty() }?.let { parts ->
            Spacer(Modifier.height(20.dp))
            PartCardExpandable(parts)
        }

        Spacer(Modifier.height(28.dp))

        OutlinedButton(
            onClick = onOpenTechnicians,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(stringResource(R.string.contacts_tech_shortcut))
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = {
            vm.restart()
            onRestartToHome()
        }) {
            Text(stringResource(R.string.diagnostic_home))
        }
    }
}

@Composable
private fun EndResultIcon(result: EndResult) {
    val (bg, icon) = when (result) {
        EndResult.RESOLVED -> Color(0xFF4CAF50) to Icons.Filled.Check
        EndResult.NO_ISSUE -> Color(0xFFFFC107) to Icons.Filled.Warning
        EndResult.COMPONENT_FAULT -> Color(0xFFE53935) to Icons.Filled.Build
    }

    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
private fun SuggestCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Suggerimenti",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            Text(text)
        }
    }
}

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
private fun PartCardExpandable(parts: List<PartRefResolved>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Piezas de recambio",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(Modifier.height(16.dp))

                parts.forEachIndexed { index, ref ->
                    Text(
                        text = ref.detail.product,
                        style = MaterialTheme.typography.titleSmall
                    )
                    ref.qty?.let { Text("Cantidad: $it") }
                    ref.detail.code?.let { Text("Código: $it") }
                    ref.detail.features?.let { Text("Características: $it") }
                    ref.detail.supplier?.let { Text("Proveedor: $it") }
                    ref.detail.technicalContacts?.let { Text("Contacto técnico: $it") }

                    Spacer(Modifier.height(12.dp))

                    ref.detail.imageResName?.let { resName ->
                        val context = LocalContext.current
                        val resId = context.resources.getIdentifier(
                            resName,
                            "drawable",
                            context.packageName
                        )
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    if (index < parts.lastIndex) {
                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
