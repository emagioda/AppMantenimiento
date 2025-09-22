package com.emagioda.myapp.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { inner ->
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeHeader()

                Spacer(Modifier.height(32.dp))

                PrimaryScanButton(onClick = onNavigateToScanner)

                Spacer(Modifier.height(36.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

                Spacer(Modifier.height(28.dp))

                HelpSection(onContactsClick = onNavigateToContacts)
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Text(
        text = "Escaneá el código QR\ny te guiaremos paso a paso\npara resolver tu problema.",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PrimaryScanButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text("Escanear QR", fontSize = 18.sp)
    }
}

@Composable
private fun HelpSection(onContactsClick: () -> Unit) {
    // Detecta si el fondo actual es oscuro midiendo luminancia
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Text(
        text = "¿Necesitás ayuda?",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 20.sp
    )
    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onContactsClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Text("Contactos", fontSize = 17.sp)
    }
}
