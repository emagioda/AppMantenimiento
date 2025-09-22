package com.emagioda.myapp.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.regex.Pattern
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars

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
        null -> {} // estado inicial
        false -> PermissionRationale { requestPermission.launch(Manifest.permission.CAMERA) }
        true -> CameraPreview(onScanned = onScanned, modifier = modifier.fillMaxSize())
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Necesitamos la cámara para escanear el QR.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) { Text("Conceder permiso") }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    onScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    var handled by remember { mutableStateOf(false) }
    val idRegex = remember { Pattern.compile("^[A-Z0-9_]{3,}$") }

    // Torch state + control
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
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
                                            Log.e("Scanner", "Error procesando imagen", e)
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, selector, preview, analysis
                        )
                        cameraControl = camera.cameraControl
                        // si la linterna estaba activa y volvemos a esta pantalla
                        cameraControl?.enableTorch(torchEnabled)
                    } catch (e: Exception) {
                        Log.e("Scanner", "Fallo al bindear use cases", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            onRelease = { analysisExecutor.shutdown() }
        )

        // Overlay del marco
        QRScannerOverlay()

        // Botón de linterna (FAB con emoji simple)
        FloatingActionButton(
            onClick = {
                torchEnabled = !torchEnabled
                cameraControl?.enableTorch(torchEnabled)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    WindowInsets.navigationBars.asPaddingValues() // 👈 evita que se solape
                )
                .padding(16.dp)
        ) {
            val icon = if (torchEnabled) "💡" else "🔦"
            Text(icon)
        }
    }
}
