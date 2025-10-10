package com.emagioda.myapp.presentation.screen.scanner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R

@Composable
fun QRScannerOverlay(
    modifier: Modifier = Modifier,
    frameSizeDp: Int = 260,
    cornerLenDp: Int = 24,
    scrimAlpha: Float = 0.55f,
    showScanLine: Boolean = true
) {
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
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val scrim = Color.Black.copy(alpha = scrimAlpha)

            drawRect(color = scrim)

            val frameW = frameSizeDp.dp.toPx()
            val frameH = frameSizeDp.dp.toPx()
            val left = (size.width - frameW) / 2f
            val top = (size.height - frameH) / 2f
            val right = left + frameW
            val bottom = top + frameH

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(frameW, frameH),
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(frameW, frameH),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            val c = cornerLenDp.dp.toPx()
            val s = 5.dp.toPx()
            val green = Color(0xFF00E676)

            drawLine(green, Offset(left, top), Offset(left + c, top), s)
            drawLine(green, Offset(left, top), Offset(left, top + c), s)
            drawLine(green, Offset(right, top), Offset(right - c, top), s)
            drawLine(green, Offset(right, top), Offset(right, top + c), s)
            drawLine(green, Offset(left, bottom), Offset(left + c, bottom), s)
            drawLine(green, Offset(left, bottom), Offset(left, bottom - c), s)
            drawLine(green, Offset(right, bottom), Offset(right - c, bottom), s)
            drawLine(green, Offset(right, bottom), Offset(right, bottom - c), s)

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

        Text(
            text = stringResource(R.string.scanner_hint),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
