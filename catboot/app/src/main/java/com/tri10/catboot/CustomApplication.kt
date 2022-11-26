package com.tri10.catboot

import android.app.Application
import com.tri10.catboot.implementation.composition.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CatbootApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CatbootApplication)
            modules(appModule)
        }
    }
}