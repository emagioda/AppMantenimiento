package com.emagioda.myapp.domain.repository

import com.emagioda.myapp.domain.model.DiagnosticTree

interface DiagnosticRepository {
    /**
     * Devuelve el árbol correspondiente a un machineId (busca en machines.json el templateId).
     * Lanza excepción con mensaje claro si no encuentra.
     */
    fun getTreeForMachine(machineId: String): DiagnosticTree
}
