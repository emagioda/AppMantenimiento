package com.emagioda.myapp.domain.model

data class Contact(
    val id: String,
    val type: ContactType,
    val name: String,
    val company: String?,
    val roles: List<String>?,
    val specialties: List<String>?,
    val phones: List<String>?,
    val whatsapp: String?,
    val emails: List<String>?,
    val location: String?,
    val coverage: List<String>?,
    val isEmergency: Boolean,
    val isFavorite: Boolean,
    val notes: String?
)

enum class ContactType { TECHNICIAN, PROVIDER }
