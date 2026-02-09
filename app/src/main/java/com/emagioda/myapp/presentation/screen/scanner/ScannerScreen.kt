@file:Suppress("DEPRECATION")

package com.emagioda.myapp.presentation.screen.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle // IMPORTANTE: Agregado para la corrección
import androidx.lifecycle.LifecycleEventObserver // IMPORTANTE: Agregado para la corrección
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.R
import com.emagioda.myapp.presentation.viewmodel.ScannerViewModel
import android.content.Intent
import android.net.Uri
import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.regex.Pattern
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.launch

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    onScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val vm: ScannerViewModel = viewModel(
        factory = ScannerViewModel.Factory(
            ServiceLocator.provideGetMachineIds(context)
        )
    )

    var hasPermission by remember { mutableStateOf<Boolean?>(null) }
    var showRationale by remember { mutableStateOf(false) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            showRationale = !granted
        }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        hasPermission = granted
        if (!granted) {
            if (!hasRequestedPermission) {
                hasRequestedPermission = true
                requestPermission.launch(Manifest.permission.CAMERA)
            } else {
                showRationale = true
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                hasPermission = granted
                showRationale = !granted && hasRequestedPermission
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when {
        hasPermission == null -> {}
        hasPermission == true -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0)
            ) { innerPadding ->

                if (vm.uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    CameraPreview(
                        machineIds = vm.uiState.machineIds,
                        onScanned = { machineId ->
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onScanned(machineId)
                        },
                        onInvalidMachine = {
                            snackbarScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.scanner_invalid_machine)
                                )
                            }
                        },
                        modifier = modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
        showRationale -> PermissionRationale()
        else -> {}
    }
}

@Composable
private fun PermissionRationale(
    liftABit: Boolean = true
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(safeInsets)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (liftABit) (-32).dp else 0.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.scanner_permission_rationale),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Button(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.findActivity()?.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.scanner_permission_button).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@ExperimentalGetImage
@Composable
private fun CameraPreview(
    machineIds: Set<String>,
    onScanned: (String) -> Unit,
    onInvalidMachine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current

    // Esta variable impide múltiples lecturas rápidas
    var handled by rememberSaveable { mutableStateOf(false) }

    // --- CORRECCIÓN: Resetear 'handled' cuando la pantalla vuelve a primer plano (ON_RESUME) ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                handled = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // ----------------------------------------------------------------------------------------

    // Controllo dello "spam" di errori
    var lastInvalidTime by rememberSaveable { mutableStateOf(0L) }
    var lastInvalidValue by rememberSaveable { mutableStateOf<String?>(null) }

    val idRegex = remember { Pattern.compile("^[A-Za-z0-9._-]{3,}$") }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    // Stato e controllo della torcia
    var torchEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    Box(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build().apply {
                            surfaceProvider = previewView.surfaceProvider
                        }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().apply {
                            setAnalyzer(analysisExecutor) { imageProxy ->
                                if (handled) { imageProxy.close(); return@setAnalyzer }
                                // Si ya se manejó un QR exitoso, no procesamos más frames hasta que se resetee

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage, imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val value = barcodes.firstOrNull()?.rawValue?.trim()
                                            if (!value.isNullOrEmpty()
                                                && idRegex.matcher(value).matches()
                                            ) {
                                                if (machineIds.contains(value)) {
                                                    handled = true
                                                    onScanned(value)
                                                } else {
                                                    val now = System.currentTimeMillis()
                                                    val shouldNotify = value != lastInvalidValue
                                                            || now - lastInvalidTime > 1500
                                                    if (shouldNotify) {
                                                        mainExecutor.execute {
                                                            lastInvalidValue = value
                                                            lastInvalidTime = now
                                                            onInvalidMachine()
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "Scanner",
                                                context.getString(R.string.scanner_processing_error),
                                                e
                                            )
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    try {
                        provider.unbindAll()
                        val camera = provider.bindToLifecycle(
                            lifecycleOwner, selector, preview, analysis
                        )
                        cameraControl = camera.cameraControl
                        cameraControl?.enableTorch(torchEnabled)
                    } catch (e: Exception) {
                        Log.e(
                            "Scanner",
                            context.getString(R.string.scanner_bind_error),
                            e
                        )
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        QRScannerOverlay()

        val torchCd = if (torchEnabled)
            stringResource(R.string.scanner_torch_on_cd)
        else
            stringResource(R.string.scanner_torch_off_cd)

        FloatingActionButton(
            onClick = {
                torchEnabled = !torchEnabled
                cameraControl?.enableTorch(torchEnabled)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(16.dp)
                .semantics { contentDescription = torchCd }
        ) {
            val icon = if (torchEnabled) "💡" else "🔦"
            Text(icon)
        }

        DisposableEffect(Unit) {
            onDispose {
                try { cameraProvider?.unbindAll() } catch (_: Exception) {}
                try { analysisExecutor.shutdown() } catch (_: Exception) {}
            }
        }
    }
}
