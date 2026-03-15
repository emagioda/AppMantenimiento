package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.model.MachineDetail
import com.emagioda.myapp.domain.repository.MachineRepository

class MachineRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : MachineRepository {
    private val machines by lazy {
        ds.readMachinesIndex().machines.map {
            MachineDetail(
                id = it.id,
                templateId = it.templateId,
                name = it.name.orEmpty(),
                description = it.description,
                imageName = it.imageName
            )
        }
    }

    override fun getMachineIds(): Set<String> {
        return machines.map { it.id }.toSet()
    }

    override fun getMachine(machineId: String): MachineDetail? =
        machines.firstOrNull { it.id == machineId }
}
