package com.emagioda.myapp.domain.usecase

import com.emagioda.myapp.domain.model.DiagnosticTree
import com.emagioda.myapp.domain.repository.DiagnosticRepository

class GetDiagnosticTreeForMachine(
    private val repo: DiagnosticRepository
) {
    operator fun invoke(machineId: String): DiagnosticTree =
        repo.getTreeForMachine(machineId)
}
