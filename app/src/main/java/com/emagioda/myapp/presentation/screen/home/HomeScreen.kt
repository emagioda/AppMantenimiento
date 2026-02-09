package com.emagioda.myapp.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emagioda.myapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title)) }
            )
        }
    ) { inner ->
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Spacer elástico superior para empujar el texto hacia abajo
                Spacer(Modifier.weight(1f))

                // Texto del encabezado (sin icono)
                HomeHeader()

                // 2. Spacer elástico inferior para equilibrar y centrar el texto verticalmente
                Spacer(Modifier.weight(1f))

                // El botón queda anclado en la parte inferior
                PrimaryScanButton(onClick = onNavigateToScanner)

                // Espacio ergonómico inferior
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    // Se eliminó el icono grande para limpiar la vista
    Text(
        text = stringResource(R.string.home_header),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun PrimaryScanButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.home_scan_button).uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}