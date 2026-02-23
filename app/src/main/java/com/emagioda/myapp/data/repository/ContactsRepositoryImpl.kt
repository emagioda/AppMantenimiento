package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsContactsDataSource
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val ds: AssetsContactsDataSource
) : ContactsRepository {

    override fun getContacts(type: ContactType): List<Contact> {
        val raws = ds.loadContacts().filter { raw ->
            when (type) {
                ContactType.TECHNICIAN -> raw.isTechnician
                ContactType.PROVIDER -> raw.isManufacturer
            }
        }

        return raws.map {
            Contact(
                id = it.id,
                type = type,
                name = it.name,
                company = it.company,
                specialties = it.specialties,
                phones = it.phones,
                whatsapp = it.whatsapp,
                emails = it.emails,
                location = it.location,
                servicearea = it.servicearea,
            )
        }.sortedBy { it.name.lowercase() }
    }
}
