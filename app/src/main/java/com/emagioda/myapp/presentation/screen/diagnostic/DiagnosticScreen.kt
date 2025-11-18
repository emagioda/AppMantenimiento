package com.emagioda.myapp.presentation.screen.diagnostic

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.*
import com.emagioda.myapp.presentation.viewmodel.DiagnosticViewModel
import kotlin.math.max

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
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        val canScroll = scrollState.maxValue > 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (canScroll) Arrangement.Top else Arrangement.Center
            ) {
                if (node == null) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.diagnostic_error_loading),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(Modifier.height(24.dp))
                    when (node.type) {
                        NodeType.QUESTION -> QuestionContent(node = node, vm = vm)
                        NodeType.END -> EndContent(
                            node = node,
                            vm = vm,
                            onRestartToHome = onRestartToHome,
                            onOpenTechnicians = onOpenTechnicians
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
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
                    Text(stringResource(R.string.diagnostic_continue))
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
        EndResultIcon(node.result ?: EndResult.NO_ISSUE)

        Spacer(Modifier.height(24.dp))

        Text(
            text = node.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        node.description?.let {
            Spacer(Modifier.height(20.dp))
            SuggestCard(it)
        }

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
                text = stringResource(R.string.diagnostic_suggestions_title),
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
                    text = stringResource(R.string.diagnostic_parts_title),
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
                    ref.qty?.let { Text(stringResource(R.string.diagnostic_part_qty_value, it)) }
                    ref.detail.code?.let { Text(stringResource(R.string.diagnostic_part_code_value, it)) }
                    ref.detail.features?.let { Text(stringResource(R.string.diagnostic_part_features_value, it)) }
                    ref.detail.supplier?.let { Text(stringResource(R.string.diagnostic_part_supplier_value, it)) }
                    ref.detail.technicalContacts?.let {
                        Text(stringResource(R.string.diagnostic_part_technical_contact_value, it))
                    }

                    Spacer(Modifier.height(12.dp))

                    ref.detail.imageResName?.let { resName ->
                        val context = LocalContext.current
                        val resId = context.resources.getIdentifier(
                            resName,
                            "drawable",
                            context.packageName
                        )
                        if (resId != 0) {
                            ZoomablePartImage(
                                resId = resId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    if (index < parts.lastIndex) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoomablePartImage(
    resId: Int,
    modifier: Modifier = Modifier
) {
    var showZoom by remember { mutableStateOf(false) }

    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = modifier.clickable { showZoom = true }
    )

    if (showZoom) {
        Dialog(
            onDismissRequest = { showZoom = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false   // fullscreen real
            )
        ) {
            ZoomableImageDialogContent(
                resId = resId,
                onClose = { showZoom = false }
            )
        }
    }
}

@Composable
private fun ZoomableImageDialogContent(
    resId: Int,
    onClose: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { boxSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 6f)

                    val maxOffsetX =
                        max(0f, boxSize.width.toFloat() * (newScale - 1f) / 2f)
                    val maxOffsetY =
                        max(0f, boxSize.height.toFloat() * (newScale - 1f) / 2f)

                    val rawOffsetX = offset.x + pan.x
                    val rawOffsetY = offset.y + pan.y

                    val clampedOffsetX = rawOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                    val clampedOffsetY = rawOffsetY.coerceIn(-maxOffsetY, maxOffsetY)

                    scale = newScale
                    offset = Offset(clampedOffsetX, clampedOffsetY)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
