package io.github.helmy2.mudawama

import android.app.Application
import io.github.helmy2.mudawama.umbrella.ui.setupModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MudawamaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MudawamaApplication)
            setupModules()
        }
    }
}