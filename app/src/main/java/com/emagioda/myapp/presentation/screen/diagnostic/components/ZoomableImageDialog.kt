package com.emagioda.myapp.presentation.screen.diagnostic.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max

@Composable
fun ZoomablePartImage(
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
fun ZoomableImageDialogContent(
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
