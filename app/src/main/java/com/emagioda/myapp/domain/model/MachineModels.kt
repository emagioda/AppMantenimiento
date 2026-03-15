package com.emagioda.myapp.domain.model

data class MachineDetail(
    val id: String,
    val templateId: String,
    val name: String,
    val description: String?,
    val imageName: String?
)
