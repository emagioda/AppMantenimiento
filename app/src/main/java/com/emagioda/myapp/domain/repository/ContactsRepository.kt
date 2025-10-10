package com.emagioda.myapp.domain.repository

import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType

interface ContactsRepository {
    fun getContacts(type: ContactType): List<Contact>
}
