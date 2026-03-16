@file:Suppress("DEPRECATION")

package com.emagioda.myapp.presentation.screen.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.presentation.viewmodel.ScannerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlinx.coroutines.launch

private enum class CameraPermissionState {
    CHECKING,
    GRANTED,
    RATIONALE,
    SETTINGS
}

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
    val activity = context.findActivity()
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val vm: ScannerViewModel = viewModel(
        factory = ScannerViewModel.Factory(
            ServiceLocator.provideGetMachineIds(context)
        )
    )

    var permissionState by rememberSaveable {
        mutableStateOf(CameraPermissionState.CHECKING.name)
    }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    fun refreshPermissionState() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        permissionState = when {
            granted -> CameraPermissionState.GRANTED.name
            !hasRequestedPermission -> CameraPermissionState.CHECKING.name
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            ) -> CameraPermissionState.RATIONALE.name
            else -> CameraPermissionState.SETTINGS.name
        }
    }

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasRequestedPermission = true
            permissionState = if (granted) {
                CameraPermissionState.GRANTED.name
            } else {
                run {
                    refreshPermissionState()
                    permissionState
                }
            }
        }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            permissionState = CameraPermissionState.GRANTED.name
        } else if (!hasRequestedPermission) {
            hasRequestedPermission = true
            requestPermission.launch(Manifest.permission.CAMERA)
        } else {
            refreshPermissionState()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (CameraPermissionState.valueOf(permissionState)) {
        CameraPermissionState.CHECKING -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        CameraPermissionState.GRANTED -> {
            val errorResId = vm.uiState.errorResId
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0)
            ) { innerPadding ->
                when {
                    vm.uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorResId != null -> {
                        ErrorState(
                            text = stringResource(errorResId),
                            buttonText = stringResource(R.string.common_retry),
                            onClick = vm::retry,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }

                    else -> {
                        CameraPreview(
                            machineIds = vm.uiState.machineIds,
                            onScanned = { machineId ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onScanned(machineId)
                            },
                            onInvalidMachine = {
                                snackbarScope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.scanner_invalid_machine)
                                    )
                                }
                            },
                            onCameraBindError = {
                                snackbarScope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.scanner_bind_error)
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
        }

        CameraPermissionState.RATIONALE -> {
            PermissionStateView(
                text = stringResource(R.string.scanner_permission_rationale),
                buttonText = stringResource(R.string.scanner_permission_retry),
                onClick = { requestPermission.launch(Manifest.permission.CAMERA) }
            )
        }

        CameraPermissionState.SETTINGS -> {
            PermissionStateView(
                text = stringResource(R.string.scanner_permission_settings_message),
                buttonText = stringResource(R.string.scanner_permission_button),
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    activity?.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun PermissionStateView(
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    liftABit: Boolean = true
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()

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
                text = text,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Button(
            onClick = onClick,
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
                text = buttonText.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorState(
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onClick) {
                Text(buttonText.uppercase())
            }
        }
    }
}

@ExperimentalGetImage
@Composable
private fun CameraPreview(
    machineIds: Set<String>,
    onScanned: (String) -> Unit,
    onInvalidMachine: () -> Unit,
    onCameraBindError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current

    var handled by rememberSaveable { mutableStateOf(false) }
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

    var lastInvalidTime by rememberSaveable { mutableStateOf(0L) }
    var lastInvalidValue by rememberSaveable { mutableStateOf<String?>(null) }

    val idRegex = remember { Pattern.compile("^[A-Za-z0-9._-]{3,}$") }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

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
                                if (handled) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val value = barcodes.firstOrNull()?.rawValue?.trim()
                                            if (!value.isNullOrEmpty() &&
                                                idRegex.matcher(value).matches()
                                            ) {
                                                if (machineIds.contains(value)) {
                                                    handled = true
                                                    onScanned(value)
                                                } else {
                                                    val now = System.currentTimeMillis()
                                                    val shouldNotify = value != lastInvalidValue ||
                                                        now - lastInvalidTime > 1500
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
                            lifecycleOwner,
                            selector,
                            preview,
                            analysis
                        )
                        cameraControl = camera.cameraControl
                        cameraControl?.enableTorch(torchEnabled)
                    } catch (e: Exception) {
                        Log.e(
                            "Scanner",
                            context.getString(R.string.scanner_bind_error),
                            e
                        )
                        mainExecutor.execute(onCameraBindError)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        QRScannerOverlay(showScanLine = false)

        val torchCd = if (torchEnabled) {
            stringResource(R.string.scanner_torch_on_cd)
        } else {
            stringResource(R.string.scanner_torch_off_cd)
        }

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
            Text(if (torchEnabled) "💡" else "🔦")
        }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    cameraProvider?.unbindAll()
                } catch (_: Exception) {
                }
                try {
                    analysisExecutor.shutdown()
                } catch (_: Exception) {
                }
            }
        }
    }
}
