package com.sap.codelab.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.LocationServices
import com.sap.codelab.utils.geofence.GeoFenceManager
import com.sap.codelab.core.data.Repository
import com.sap.codelab.core.data.db.AppDatabase
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.create.presentation.CreateMemoViewModel
import com.sap.codelab.detail.presentation.ViewMemoViewModel
import com.sap.codelab.home.domain.HomeUseCases
import com.sap.codelab.home.domain.LoadMemosUseCase
import com.sap.codelab.home.domain.SaveMemoUseCase
import com.sap.codelab.home.presentation.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    single<AppDatabase> {
        AppDatabase.getInstance(androidContext())
    }

    single { Repository(get()) } bind IMemoRepository::class
    single { HomeUseCases(SaveMemoUseCase(get ()), LoadMemosUseCase(get()))}
    single { LocationServices.getGeofencingClient(androidContext()) }
    single { GeoFenceManager(androidContext(), get()) }
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateMemoViewModel)
    viewModelOf(::ViewMemoViewModel)
}
