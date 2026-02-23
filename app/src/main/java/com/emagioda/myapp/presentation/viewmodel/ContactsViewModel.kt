package com.emagioda.myapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.usecase.GetContacts

class ContactsViewModel(
    private val getContacts: GetContacts,
    providerIds: String? = null,
    technicianIds: String? = null
) : ViewModel() {

    private val providerIdFilter = providerIds.toIdSet()
    private val technicianIdFilter = technicianIds.toIdSet()

    fun technicians(): List<Contact> {
        val contacts = getContacts(ContactType.TECHNICIAN)
        return if (technicianIdFilter.isEmpty()) contacts else contacts.filter { it.id in technicianIdFilter }
    }

    fun providers(): List<Contact> {
        val contacts = getContacts(ContactType.PROVIDER)
        return if (providerIdFilter.isEmpty()) contacts else contacts.filter { it.id in providerIdFilter }
    }

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
