package com.emagioda.myapp.ui.scanner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun QRScannerOverlay(
    modifier: Modifier = Modifier,
    frameSizeDp: Int = 260,
    cornerLenDp: Int = 24,
    scrimAlpha: Float = 0.55f,
    showScanLine: Boolean = true
) {
    // Animación de la línea (esto SÍ va en un contexto composable)
    val yAnim: Float = if (showScanLine) {
        val infinite = rememberInfiniteTransition(label = "scan")
        val anim by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scanY"
        )
        anim
    } else 0f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Necesario para que BlendMode.Clear haga el “agujero”
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val scrim = Color.Black.copy(alpha = scrimAlpha)

            // 1) Scrim completo
            drawRect(color = scrim)

            // Medidas del cuadro centrado
            val frameW = frameSizeDp.dp.toPx()
            val frameH = frameSizeDp.dp.toPx()
            val left = (size.width - frameW) / 2f
            val top = (size.height - frameH) / 2f
            val right = left + frameW
            val bottom = top + frameH

            // 2) Agujero sin filtro dentro del cuadro
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(frameW, frameH),
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // 3) Borde blanco del cuadro
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(frameW, frameH),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // 4) Esquinas verdes
            val c = cornerLenDp.dp.toPx()
            val s = 5.dp.toPx()
            val green = Color(0xFF00E676)

            // sup-izq
            drawLine(green, Offset(left, top), Offset(left + c, top), s)
            drawLine(green, Offset(left, top), Offset(left, top + c), s)
            // sup-der
            drawLine(green, Offset(right, top), Offset(right - c, top), s)
            drawLine(green, Offset(right, top), Offset(right, top + c), s)
            // inf-izq
            drawLine(green, Offset(left, bottom), Offset(left + c, bottom), s)
            drawLine(green, Offset(left, bottom), Offset(left, bottom - c), s)
            // inf-der
            drawLine(green, Offset(right, bottom), Offset(right - c, bottom), s)
            drawLine(green, Offset(right, bottom), Offset(right, bottom - c), s)

            // 5) Línea de escaneo (usa el valor ya animado)
            if (showScanLine) {
                val yPos = top + frameH * yAnim
                drawLine(
                    color = green,
                    start = Offset(left, yPos),
                    end = Offset(right, yPos),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

//        Texto de ayuda
//        Text(
//            text = "Alineá el QR dentro del cuadro",
//            color = Color.White,
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 32.dp)
//        )
    }
}
