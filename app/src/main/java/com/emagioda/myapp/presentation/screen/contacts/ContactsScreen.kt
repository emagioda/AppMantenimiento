package com.emagioda.myapp.presentation.screen.contacts

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    initialTab: Int = 0 // 0 = Técnicos, 1 = Proveedores
) {
    val context = LocalContext.current
    val vm: ContactsViewModel = viewModel(
        factory = ContactsViewModel.Factory(ServiceLocator.provideGetContacts(context))
    )

    var tab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    val tabs = listOf(
        stringResource(R.string.contacts_tab_technicians),
        stringResource(R.string.contacts_tab_providers)
    )
    val items: List<Contact> = if (tab == 0) vm.technicians() else vm.providers()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        selectedContentColor = if (isDark) Color.White else TabRowDefaults.primaryContentColor,
                        unselectedContentColor = if (isDark) Color.White.copy(alpha = 0.7f) else TabRowDefaults.secondaryContentColor,
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (items.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(items = items, key = { c -> c.id }) { c ->
                        ContactCard(
                            contact = c,
                            onCall = { phone ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
                            },
                            onWhatsApp = { number ->
                                val clean = number.filter { it.isDigit() || it == '+' }
                                context.startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$clean".toUri()))
                            },
                            onEmail = { email ->
                                context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.contacts_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.contacts_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onEmail: (String) -> Unit
) {
    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (contact.isEmergency) {
                    Text(
                        text = stringResource(R.string.contacts_emergency),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (contact.isFavorite) {
                    Text(
                        text = stringResource(R.string.contacts_favorite),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            contact.company?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
            contact.specialties?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            contact.location?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val phone = contact.phones?.firstOrNull()
                val wa = contact.whatsapp
                val email = contact.emails?.firstOrNull()

                if (!phone.isNullOrBlank()) {
                    AssistChip(onClick = { onCall(phone) }, label = { Text(stringResource(R.string.contacts_action_call)) })
                }
                if (!wa.isNullOrBlank()) {
                    AssistChip(onClick = { onWhatsApp(wa) }, label = { Text(stringResource(R.string.contacts_action_whatsapp)) })
                }
                if (!email.isNullOrBlank()) {
                    AssistChip(onClick = { onEmail(email) }, label = { Text(stringResource(R.string.contacts_action_email)) })
                }
            }
        }
    }
}
