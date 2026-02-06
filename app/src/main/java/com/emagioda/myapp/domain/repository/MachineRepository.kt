package com.emagioda.myapp.domain.repository

interface MachineRepository {
    fun getMachineIds(): Set<String>
}
