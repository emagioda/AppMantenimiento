package com.emagioda.myapp.di

import android.content.Context
import com.emagioda.myapp.data.datasource.AssetsDiagnosticDataSource
import com.emagioda.myapp.data.repository.DiagnosticRepositoryImpl
import com.emagioda.myapp.domain.repository.DiagnosticRepository
import com.emagioda.myapp.domain.usecase.GetDiagnosticTreeForMachine
import com.google.gson.Gson
import com.emagioda.myapp.data.datasource.AssetsContactsDataSource
import com.emagioda.myapp.data.repository.ContactsRepositoryImpl
import com.emagioda.myapp.domain.repository.ContactsRepository
import com.emagioda.myapp.domain.usecase.GetContacts

object ServiceLocator {

    fun provideDiagnosticRepository(context: Context): DiagnosticRepository {
        val ds = AssetsDiagnosticDataSource(context, Gson())
        return DiagnosticRepositoryImpl(ds)
    }

    fun provideGetTreeUseCase(context: Context): GetDiagnosticTreeForMachine {
        return GetDiagnosticTreeForMachine(provideDiagnosticRepository(context))
    }

    fun provideContactsRepository(context: Context): ContactsRepository {
        val ds = AssetsContactsDataSource(context, Gson())
        return ContactsRepositoryImpl(ds)
    }

    fun provideGetContacts(context: Context): GetContacts {
        return GetContacts(provideContactsRepository(context))
    }
}
