package com.emagioda.myapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.usecase.GetContacts

class ContactsViewModel(
    private val getContacts: GetContacts
) : ViewModel() {

    fun technicians(): List<Contact> = getContacts(ContactType.TECHNICIAN)
    fun providers(): List<Contact> = getContacts(ContactType.PROVIDER)

    class Factory(private val getContacts: GetContacts) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsViewModel(getContacts) as T
        }
    }
}
