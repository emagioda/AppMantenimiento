package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.usecase.GetContacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContactsUiState(
    val technicians: List<Contact> = emptyList(),
    val providers: List<Contact> = emptyList(),
    val isLoading: Boolean = true,
    val errorResId: Int? = null
)

class ContactsViewModel(
    private val getContacts: GetContacts,
    providerIds: String? = null,
    technicianIds: String? = null
) : ViewModel() {

    private val providerIdFilter = providerIds.toIdSet()
    private val technicianIdFilter = technicianIds.toIdSet()

    var uiState by mutableStateOf(ContactsUiState())
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val technicians = getContacts(ContactType.TECHNICIAN)
                    .filterIfNeeded(technicianIdFilter)
                val providers = getContacts(ContactType.PROVIDER)
                    .filterIfNeeded(providerIdFilter)

                withContext(Dispatchers.Main) {
                    uiState = ContactsUiState(
                        technicians = technicians,
                        providers = providers,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = ContactsUiState(
                        isLoading = false,
                        errorResId = R.string.contacts_error_loading
                    )
                }
            }
        }
    }

    private fun List<Contact>.filterIfNeeded(ids: Set<String>): List<Contact> =
        if (ids.isEmpty()) this else filter { it.id in ids }

    private fun String?.toIdSet(): Set<String> = this
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()

    class Factory(
        private val getContacts: GetContacts,
        private val providerIds: String? = null,
        private val technicianIds: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsViewModel(getContacts, providerIds, technicianIds) as T
        }
    }
}
