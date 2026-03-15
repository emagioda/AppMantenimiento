package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.MachineDetail
import com.emagioda.myapp.domain.repository.MachineRepository

class GetMachineDetail(
    private val repo: MachineRepository
) {
    operator fun invoke(machineId: String): MachineDetail? = repo.getMachine(machineId)
}
