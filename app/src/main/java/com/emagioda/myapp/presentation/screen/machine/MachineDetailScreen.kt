package com.emagioda.myapp.presentation.screen.machine

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emagioda.myapp.R
import com.google.gson.Gson

// --------- MODELLI / CARICAMENTO JSON (Sin cambios) ---------

private data class MachinesWrapper(
    val machines: List<MachineJson>
)

private data class MachineJson(
    val id: String,
    val templateId: String,
    val name: String,
    val description: String? = null,
    val imageName: String? = null
)

private fun loadMachineFromAssets(context: Context, machineId: String): MachineJson? {
    return try {
        val json = context.assets.open("machines.json")
            .bufferedReader()
            .use { it.readText() }

        val wrapper = Gson().fromJson(json, MachinesWrapper::class.java)
        wrapper.machines.firstOrNull { it.id == machineId }
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    machineId: String,
    onBack: () -> Unit,
    onStartDiagnostic: (String) -> Unit
) {
    val context = LocalContext.current

    val machine = remember(machineId) {
        loadMachineFromAssets(context, machineId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = machine?.name ?: stringResource(R.string.diagnostic_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            if (machine == null) {
                Text(
                    text = stringResource(R.string.diagnostic_error_loading),
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            // --- CAMBIOS APLICADOS AQUÍ ---
            // Se centra la columna en el Box y se deja padding solo abajo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center) // Centrado vertical en la pantalla
                    .padding(bottom = 100.dp), // Espacio para que no choque con el botón
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Immagine
                machine.imageName?.let { imageName ->
                    val resId = remember(imageName) {
                        context.resources.getIdentifier(
                            imageName,
                            "drawable",
                            context.packageName
                        )
                    }

                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = machine.name,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Descrizione
                machine.description?.let {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // --- BOTÓN ESTILO INDUSTRIAL ---
            Button(
                onClick = { onStartDiagnostic(machineId) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 40.dp) // Zona segura ergonómica
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.machine_detail_start).uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}