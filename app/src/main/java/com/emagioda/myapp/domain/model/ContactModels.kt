package com.emagioda.myapp.domain.model

data class Contact(
    val id: String,
    val type: ContactType,
    val company: String?,
    val specialties: List<String>?,
    val phones: List<String>?,
    val whatsapp: String?,
    val emails: List<String>?,
    val serviceArea: List<String>?,
)

enum class ContactType { TECHNICIAN, PROVIDER }
