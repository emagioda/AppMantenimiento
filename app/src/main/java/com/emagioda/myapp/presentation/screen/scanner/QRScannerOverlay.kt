package com.emagioda.myapp.presentation.screen.scanner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R

@Composable
fun QRScannerOverlay(
    modifier: Modifier = Modifier,
    frameSizeDp: Int = 260,
    cornerLenDp: Int = 30,
    scrimAlpha: Float = 0.6f,
    showScanLine: Boolean = true,
    laserColor: Color = Color(0xFFFF3D00),
    cornerColor: Color = Color(0xFF00E676)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")

    // Animación de posición Y (ida y vuelta)
    val yAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanY"
    )

    // Animación de opacidad del láser
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val scrim = Color.Black.copy(alpha = scrimAlpha)
            val cornerRadius = 16.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val cornerLength = cornerLenDp.dp.toPx()

            // 1. Dibujar fondo oscuro
            drawRect(color = scrim)

            val frameSize = frameSizeDp.dp.toPx()
            val left = (size.width - frameSize) / 2f
            val top = (size.height - frameSize) / 2f
            val right = left + frameSize
            val bottom = top + frameSize

            // 2. Recortar el agujero
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(cornerRadius),
                blendMode = BlendMode.Clear
            )

            // 3. Dibujar borde blanco sutil
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Dibujar Esquinas curvas
            val path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)

                moveTo(right - cornerLength, top)
                lineTo(right - cornerRadius, top)
                quadraticTo(right, top, right, top + cornerRadius)
                lineTo(right, top + cornerLength)

                moveTo(right, bottom - cornerLength)
                lineTo(right, bottom - cornerRadius)
                quadraticTo(right, bottom, right - cornerRadius, bottom)
                lineTo(right - cornerLength, bottom)

                moveTo(left + cornerLength, bottom)
                lineTo(left + cornerRadius, bottom)
                quadraticTo(left, bottom, left, bottom - cornerRadius)
                lineTo(left, bottom - cornerLength)
            }

            drawPath(
                path = path,
                color = cornerColor,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // 5. Dibujar Línea de Escaneo
            if (showScanLine) {
                val yPos = top + (frameSize * yAnim)

                val brush = Brush.horizontalGradient(
                    0.0f to Color.Transparent,
                    0.1f to laserColor.copy(alpha = alphaAnim * 0.5f),
                    0.5f to laserColor.copy(alpha = alphaAnim),
                    0.9f to laserColor.copy(alpha = alphaAnim * 0.5f),
                    1.0f to Color.Transparent,
                    startX = left,
                    endX = right
                )

                drawLine(
                    brush = brush,
                    start = Offset(left, yPos),
                    end = Offset(right, yPos),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        // TEXTO DE AYUDA - POSICIONADO RELATIVO AL MARCO
        Text(
            text = stringResource(R.string.scanner_hint),
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center) // 1. Lo centramos en la pantalla (igual que el marco)
                // 2. Lo bajamos: Mitad del tamaño del marco (130dp) + un margen extra (50dp)
                .offset(y = (frameSizeDp.dp / 2) + 50.dp)
        )
    }
}