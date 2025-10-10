package com.emagioda.myapp.domain.repository

import com.emagioda.myapp.domain.model.DiagnosticTree

interface DiagnosticRepository {
    fun getTreeForMachine(machineId: String): DiagnosticTree
}