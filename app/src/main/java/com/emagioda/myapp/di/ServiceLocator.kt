package com.emagioda.myapp.di

import android.content.Context
import androidx.room.Room
import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.data.local.history.MAINTENANCE_HISTORY_MIGRATION_1_2
import com.emagioda.myapp.data.local.history.MaintenanceHistoryDatabase
import com.emagioda.myapp.data.repository.DiagnosticRepositoryImpl
import com.emagioda.myapp.data.repository.MaintenanceHistoryRepositoryImpl
import com.emagioda.myapp.data.repository.MachineRepositoryImpl
import com.emagioda.myapp.domain.repository.MaintenanceHistoryRepository
import com.emagioda.myapp.domain.repository.DiagnosticRepository
import com.emagioda.myapp.domain.repository.MachineRepository
import com.emagioda.myapp.domain.usecase.AddMaintenanceEvent
import com.emagioda.myapp.domain.usecase.CreateMaintenanceCase
import com.emagioda.myapp.domain.usecase.CancelMaintenanceCase
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine
import com.emagioda.myapp.domain.usecase.ObserveMaintenanceCaseDetail
import com.emagioda.myapp.domain.usecase.ObserveMaintenanceCases
import com.emagioda.myapp.domain.usecase.ReopenMaintenanceCase
import com.emagioda.myapp.domain.usecase.ResolveMaintenanceCase
import com.emagioda.myapp.domain.usecase.UpdateMaintenanceCase
import com.emagioda.myapp.domain.usecase.GetMachineDetail
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
    private val maintenanceHistoryDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            MaintenanceHistoryDatabase::class.java,
            "maintenance_history.db"
        )
            .addMigrations(MAINTENANCE_HISTORY_MIGRATION_1_2)
            .build()
    }
    private val maintenanceHistoryRepository by lazy {
        MaintenanceHistoryRepositoryImpl(maintenanceHistoryDatabase)
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

    fun provideGetMachineDetail(context: Context): GetMachineDetail {
        return GetMachineDetail(provideMachineRepository(context))
    }

    fun provideMaintenanceHistoryRepository(context: Context): MaintenanceHistoryRepository {
        init(context)
        return maintenanceHistoryRepository
    }

    fun provideObserveMaintenanceCases(context: Context): ObserveMaintenanceCases {
        return ObserveMaintenanceCases(provideMaintenanceHistoryRepository(context))
    }

    fun provideObserveMaintenanceCaseDetail(context: Context): ObserveMaintenanceCaseDetail {
        return ObserveMaintenanceCaseDetail(provideMaintenanceHistoryRepository(context))
    }

    fun provideCreateMaintenanceCase(context: Context): CreateMaintenanceCase {
        return CreateMaintenanceCase(provideMaintenanceHistoryRepository(context))
    }

    fun provideAddMaintenanceEvent(context: Context): AddMaintenanceEvent {
        return AddMaintenanceEvent(provideMaintenanceHistoryRepository(context))
    }

    fun provideResolveMaintenanceCase(context: Context): ResolveMaintenanceCase {
        return ResolveMaintenanceCase(provideMaintenanceHistoryRepository(context))
    }

    fun provideUpdateMaintenanceCase(context: Context): UpdateMaintenanceCase {
        return UpdateMaintenanceCase(provideMaintenanceHistoryRepository(context))
    }

    fun provideReopenMaintenanceCase(context: Context): ReopenMaintenanceCase {
        return ReopenMaintenanceCase(provideMaintenanceHistoryRepository(context))
    }

    fun provideCancelMaintenanceCase(context: Context): CancelMaintenanceCase {
        return CancelMaintenanceCase(provideMaintenanceHistoryRepository(context))
    }

    fun provideContactsRepository(context: Context): ContactsRepository {
        init(context)
        return contactsRepository
    }

    fun provideGetContacts(context: Context): GetContacts {
        return GetContacts(provideContactsRepository(context))
    }
}
