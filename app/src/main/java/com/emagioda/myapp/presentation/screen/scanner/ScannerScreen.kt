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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.emagioda.myapp.R
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

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    onScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf<Boolean?>(null) }
    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission == false) requestPermission.launch(Manifest.permission.CAMERA)
    }

    when (hasPermission) {
        null -> {}
        false -> PermissionRationale(
            onRequest = { requestPermission.launch(Manifest.permission.CAMERA) }
        )
        true -> CameraPreview(onScanned = onScanned, modifier = modifier.fillMaxSize())
    }
}

@Composable
private fun PermissionRationale(
    onRequest: () -> Unit,
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
                // Lo levantamos un poco para que no lo tape el diálogo del permiso
                .offset(y = if (liftABit) (-32).dp else 0.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.scanner_permission_rationale),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRequest) {
                Text(stringResource(R.string.scanner_permission_button))
            }
        }
    }
}

@ExperimentalGetImage
@Composable
private fun CameraPreview(
    onScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current

    var handled by rememberSaveable { mutableStateOf(false) }
    val idRegex = remember { Pattern.compile("^[A-Za-z0-9._-]{3,}$") }

    // Torch state + control
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
                                                handled = true
                                                onScanned(value)
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
            },
            onRelease = { analysisExecutor.shutdown() }
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
