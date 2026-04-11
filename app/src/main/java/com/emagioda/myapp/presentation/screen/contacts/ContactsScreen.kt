package com.emagioda.myapp.presentation.screen.contacts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emagioda.myapp.R
import com.emagioda.myapp.di.ServiceLocator
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.presentation.common.InitialsBadge
import com.emagioda.myapp.presentation.common.PremiumEmptyState
import com.emagioda.myapp.presentation.common.PremiumHeroCard
import com.emagioda.myapp.presentation.common.PremiumScreenBackground
import com.emagioda.myapp.presentation.common.PremiumSectionEyebrow
import com.emagioda.myapp.presentation.viewmodel.ContactsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    val uiState = vm.uiState
    val tabs = listOf(
        stringResource(R.string.contacts_tab_technicians),
        stringResource(R.string.contacts_tab_providers)
    )
    val safeInitialTab = initialTab.coerceIn(0, tabs.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = safeInitialTab,
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val actionErrorMessage = stringResource(R.string.contacts_external_action_error)

    fun launchIntent(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(actionErrorMessage)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                }
            )
        }
    ) { innerPadding ->
        PremiumScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PremiumHeroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    PremiumSectionEyebrow(text = stringResource(R.string.contacts_overline))
                    SegmentedTabControl(
                        tabs = tabs,
                        selectedIndex = pagerState.currentPage,
                        onSelect = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.errorResId != null -> {
                        PremiumEmptyState(
                            title = stringResource(R.string.contacts_title),
                            subtitle = stringResource(uiState.errorResId),
                            overline = stringResource(R.string.contacts_overline)
                        )
                    }

                    else -> {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val pageContacts = if (page == 0) {
                                uiState.technicians
                            } else {
                                uiState.providers
                            }

                            if (pageContacts.isEmpty()) {
                                EmptyContactsState()
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = pageContacts,
                                        key = { it.id }
                                    ) { contact ->
                                        ContactCard(
                                            contact = contact,
                                            onCall = { phone ->
                                                launchIntent(
                                                    Intent(Intent.ACTION_DIAL).apply {
                                                        data = "tel:$phone".toUri()
                                                    }
                                                )
                                            },
                                            onWhatsApp = { number ->
                                                launchIntent(
                                                    Intent(Intent.ACTION_VIEW).apply {
                                                        data = "https://api.whatsapp.com/send?phone=$number".toUri()
                                                    }
                                                )
                                            },
                                            onEmail = { email ->
                                                launchIntent(
                                                    Intent(Intent.ACTION_SENDTO).apply {
                                                        data = "mailto:$email".toUri()
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedTabControl(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContactsState() {
    PremiumEmptyState(
        title = stringResource(R.string.contacts_empty_title),
        subtitle = stringResource(R.string.contacts_empty_subtitle),
        overline = stringResource(R.string.contacts_overline)
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ContactCard(
    contact: Contact,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onEmail: (String) -> Unit
) {
    val displayName = contact.company?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.contacts_default_name)
    val initials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString("") { it.first().uppercase() }
        .take(2)

    PremiumHeroCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (contact.type == ContactType.TECHNICIAN) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        },
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InitialsBadge(text = initials)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PremiumSectionEyebrow(
                    text = if (contact.type == ContactType.TECHNICIAN) {
                        stringResource(R.string.contacts_tab_technicians)
                    } else {
                        stringResource(R.string.contacts_tab_providers)
                    }
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                contact.specialties?.takeIf { it.isNotEmpty() }?.let { specialties ->
                    Text(
                        text = specialties.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        contact.serviceArea?.takeIf { it.isNotEmpty() }?.let { areas ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.contacts_service_areas_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    areas.forEach { area ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = area,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.contacts_quick_actions),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

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
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
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
            style = MaterialTheme.typography.labelLarge
        )
    }
}
