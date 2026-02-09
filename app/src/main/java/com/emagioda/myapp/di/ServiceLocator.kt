package com.emagioda.myapp.di

import android.content.Context
import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.data.repository.DiagnosticRepositoryImpl
import com.emagioda.myapp.data.repository.MachineRepositoryImpl
import com.emagioda.myapp.domain.repository.DiagnosticRepository
import com.emagioda.myapp.domain.repository.MachineRepository
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine
import com.emagioda.myapp.domain.usecase.GetMachineIds
import com.google.gson.Gson
import com.emagioda.myapp.data.datasource.AssetsContactsDataSource
import com.emagioda.myapp.data.repository.ContactsRepositoryImpl
import com.emagioda.myapp.domain.repository.ContactsRepository
import com.emagioda.myapp.domain.usecase.GetContacts

object ServiceLocator {

    private val gson by lazy { Gson() }
    private lateinit var appContext: Context
    private val diagnosticRepository by lazy {
        DiagnosticRepositoryImpl(AssetsDiagnosticDataSource(appContext, gson))
    }
    private val machineRepository by lazy {
        MachineRepositoryImpl(AssetsDiagnosticDataSource(appContext, gson))
    }
    private val contactsRepository by lazy {
        ContactsRepositoryImpl(AssetsContactsDataSource(appContext, gson))
    }

    private fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    fun provideDiagnosticRepository(context: Context): DiagnosticRepository {
        init(context)
        return diagnosticRepository
    }

    fun provideGetTreeUseCase(context: Context): GetDiagnosticTreeForMachine {
        return GetDiagnosticTreeForMachine(provideDiagnosticRepository(context))
    }

    fun provideMachineRepository(context: Context): MachineRepository {
        init(context)
        return machineRepository
    }

    fun provideGetMachineIds(context: Context): GetMachineIds {
        return GetMachineIds(provideMachineRepository(context))
    }

    fun provideContactsRepository(context: Context): ContactsRepository {
        init(context)
        return contactsRepository
    }

    fun provideGetContacts(context: Context): GetContacts {
        return GetContacts(provideContactsRepository(context))
    }
}
