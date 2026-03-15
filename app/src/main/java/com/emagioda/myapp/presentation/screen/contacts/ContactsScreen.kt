package com.emagioda.myapp.presentation.screen.contacts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call

import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.presentation.viewmodel.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: (() -> Unit)? = null,
    initialTab: Int = 0,
    providerIds: String? = null,
    technicianIds: String? = null
) {
    val context = LocalContext.current

    val vm: ContactsViewModel = viewModel(
        factory = ContactsViewModel.Factory(
            getContacts = ServiceLocator.provideGetContacts(context),
            providerIds = providerIds,
            technicianIds = technicianIds
        )
    )

    val tabs = listOf(
        stringResource(R.string.contacts_tab_technicians),
        stringResource(R.string.contacts_tab_providers)
    )

    val safeInitialTab = initialTab.coerceIn(0, tabs.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeInitialTab, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.contacts_title)) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back)
                                )
                            }
                        }
                    }
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    indicator = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        Color.Transparent
                                    }
                                ),
                            text = {
                                // Destacar visualmente el tab seleccionado
                                Text(
                                    title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageContacts = if (page == 0) vm.technicians() else vm.providers()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
                ) {
                    items(pageContacts) { contact ->
                        ContactCard(
                            contact = contact,
                            onCall = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = "tel:$phone".toUri()
                                }
                                context.startActivity(intent)
                            },
                            onWhatsApp = { number ->
                                val url = "https://api.whatsapp.com/send?phone=$number"
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = url.toUri()
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            },
                            onEmail = { email ->
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:$email".toUri()
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onEmail: (String) -> Unit
) {
    val listSeparator = stringResource(R.string.contacts_list_separator)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // OJO: Si el texto es blanco, este fondo debería ser oscuro para que se lea.
            // Si usas el tema por defecto, quizás quieras forzar un color oscuro aquí, ej:
            // containerColor = MaterialTheme.colorScheme.primaryContainer
            // O déjalo como 'surfaceContainer' si tu tema ya es oscuro.
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            contact.company?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            // CAMBIO 4: Especialidades y Ubicación en BLANCO
            contact.specialties?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it.joinToString(listSeparator),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            contact.serviceArea?.takeIf { it.isNotEmpty() }?.let { areas ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.contacts_service_areas_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = areas.joinToString(listSeparator),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            // --- BOTONES DE ACCIÓN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val phone = contact.phones?.firstOrNull()
                val wa = contact.whatsapp
                val email = contact.emails?.firstOrNull()

                if (!phone.isNullOrBlank()) {
                    ActionButton(
                        text = stringResource(R.string.contacts_action_call),
                        icon = Icons.Default.Call,
                        onClick = { onCall(phone) }
                    )
                }

                if (!wa.isNullOrBlank()) {
                    ActionButton(
                        text = stringResource(R.string.contacts_action_whatsapp),
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = { onWhatsApp(wa) }
                    )
                }

                if (!email.isNullOrBlank()) {
                    ActionButton(
                        text = stringResource(R.string.contacts_action_email),
                        icon = Icons.Default.Email,
                        onClick = { onEmail(email) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 4.dp),
        // CAMBIO 5: Forzar el contenido del botón a BLANCO
        colors = ButtonDefaults.filledTonalButtonColors(
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.sp
        )
    }
}
