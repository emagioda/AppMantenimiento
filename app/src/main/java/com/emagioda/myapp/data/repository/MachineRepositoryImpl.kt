package com.emagioda.myapp.data.repository

import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.domain.repository.MachineRepository

class MachineRepositoryImpl(
    private val ds: AssetsDiagnosticDataSource
) : MachineRepository {
    override fun getMachineIds(): Set<String> {
        return ds.readMachinesIndex().machines.map { it.id }.toSet()
    }
}
