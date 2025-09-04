package com.sap.codelab

import android.app.Application
import com.sap.codelab.core.data.Repository

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
    }
}