package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.repository.MachineRepository

class GetMachineIds(
    private val repo: MachineRepository
) {
    operator fun invoke(): Set<String> = repo.getMachineIds()
}
