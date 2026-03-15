package com.emagioda.myapp.domain.repository

import com.emagioda.myapp.domain.model.MachineDetail

interface MachineRepository {
    fun getMachineIds(): Set<String>
    fun getMachine(machineId: String): MachineDetail?
}
