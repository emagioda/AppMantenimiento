package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsContactsDataSource
import com.emagioda.myapp.domain.model.AssetContentException
import com.emagioda.myapp.domain.model.Contact
import com.emagioda.myapp.domain.model.ContactType
import com.emagioda.myapp.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val ds: AssetsContactsDataSource
) : ContactsRepository {
    private val providerContacts by lazy {
        buildContacts(
            type = ContactType.PROVIDER,
            predicate = { it.isManufacturer }
        )
    }

    private val technicianContacts by lazy {
        buildContacts(
            type = ContactType.TECHNICIAN,
            predicate = { it.isTechnician }
        )
    }

    override fun getContacts(type: ContactType): List<Contact> {
        return when (type) {
            ContactType.TECHNICIAN -> technicianContacts
            ContactType.PROVIDER -> providerContacts
        }
    }

    private fun buildContacts(
        type: ContactType,
        predicate: (AssetsContactsDataSource.ContactRaw) -> Boolean
    ): List<Contact> {
        val raws = ds.loadContacts()
        val duplicateIds = raws
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        if (duplicateIds.isNotEmpty()) {
            throw AssetContentException(
                "Duplicate contact ids: ${duplicateIds.joinToString()}"
            )
        }

        return raws.filter(predicate).map {
            Contact(
                id = it.id,
                type = type,
                company = it.company,
                specialties = it.specialties,
                phones = it.phones,
                whatsapp = it.whatsapp,
                emails = it.emails,
                serviceArea = it.seviceArea,
            )
        }.sortedBy { it.company?.lowercase() ?: it.id.lowercase() }
    }
}
