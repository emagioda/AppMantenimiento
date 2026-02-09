package com.emagioda.myapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun SafetyWarningDialog(
    onConfirm: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        for (second in 5 downTo 1) {
            remainingSeconds = second
            delay(1_000)
        }
        remainingSeconds = 0
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFD600))
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROTOCOLO DE SEGURIDAD REQUERIDO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "RIESGO ELÉCTRICO / MECÁNICO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Está a punto de acceder a una sección que requiere intervención técnica.\n\n" +
                            "SOLO PERSONAL AUTORIZADO.\n\n" +
                            "Asegúrese de aplicar el procedimiento de bloqueo (LOTO) y verificar ausencia de tensión antes de continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    val buttonEnabled = remainingSeconds == 0
                    val buttonText = if (buttonEnabled) {
                        "ENTENDIDO, CONTINUAR"
                    } else {
                        "Habilitar confirmación (${remainingSeconds})..."
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD600),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFFFFD600).copy(alpha = 0.5f),
                            disabledContentColor = Color.Black.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
