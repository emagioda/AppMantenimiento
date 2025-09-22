package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.repository.ContactsRepository

class GetContacts(private val repo: ContactsRepository) {
    operator fun invoke(type: ContactType): List<Contact> = repo.getContacts(type)
}
