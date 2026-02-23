package com.emagioda.myapp.domain.model

data class Contact(
    val id: String,
    val type: ContactType,
    val name: String,
    val company: String?,
    val specialties: List<String>?,
    val phones: List<String>?,
    val whatsapp: String?,
    val emails: List<String>?,
    val location: String?,
    val servicearea: List<String>?,
)

enum class ContactType { TECHNICIAN, PROVIDER }
