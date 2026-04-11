package com.emagioda.myapp.presentation.screen.scanner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
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
    frameSizeDp: Int = 264,
    cornerLenDp: Int = 34,
    scrimAlpha: Float = 0.62f,
    showScanLine: Boolean = true,
    laserColor: Color = Color(0xFFFF8B38),
    cornerColor: Color = Color(0xFF78E3FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner-overlay")
    val yAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner-line-y"
    )
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner-line-alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            val scrim = Color.Black.copy(alpha = scrimAlpha)
            val cornerRadius = 20.dp.toPx()
            val innerRadius = 18.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val cornerLength = cornerLenDp.dp.toPx()

            drawRect(color = scrim)

            val frameSize = frameSizeDp.dp.toPx()
            val left = (size.width - frameSize) / 2f
            val top = (size.height - frameSize) / 2f
            val right = left + frameSize
            val bottom = top + frameSize

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(cornerRadius),
                blendMode = BlendMode.Clear
            )

            drawRoundRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(left, top),
                size = Size(frameSize, frameSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        cornerColor.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(left + 10.dp.toPx(), top + 10.dp.toPx()),
                size = Size(frameSize - 20.dp.toPx(), frameSize - 20.dp.toPx()),
                cornerRadius = CornerRadius(innerRadius)
            )

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
                color = cornerColor.copy(alpha = 0.34f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = cornerColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (showScanLine) {
                val yPos = top + (frameSize * yAnim)
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        laserColor.copy(alpha = alphaAnim * 0.55f),
                        laserColor.copy(alpha = alphaAnim),
                        laserColor.copy(alpha = alphaAnim * 0.55f),
                        Color.Transparent
                    ),
                    startX = left,
                    endX = right
                )

                drawLine(
                    brush = brush,
                    start = Offset(left + 12.dp.toPx(), yPos),
                    end = Offset(right - 12.dp.toPx(), yPos),
                    strokeWidth = 4.dp.toPx()
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (frameSizeDp.dp / 2) + 74.dp)
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = stringResource(R.string.scanner_hint),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
