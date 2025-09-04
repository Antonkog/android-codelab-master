package com.sap.codelab.di

import androidx.room.Room
import com.sap.codelab.core.data.Repository
import com.sap.codelab.core.data.db.Database
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.create.presentation.CreateMemoViewModel
import com.sap.codelab.detail.presentation.ViewMemoViewModel
import com.sap.codelab.home.presentation.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val DATABASE_NAME: String = "codelab"

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), Database::class.java, DATABASE_NAME)
            .build()
    }
    // If you want to expose Dao as well, uncomment next line
    // single { get<Database>().getMemoDao() }

    single { Repository(get()) } bind IMemoRepository::class

    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateMemoViewModel)
    viewModelOf(::ViewMemoViewModel)
}
