package me.amitshekhar.learn.kotlin.coroutines

import android.app.Application
import me.amitshekhar.learn.kotlin.coroutines.di.module.appModule
import me.amitshekhar.learn.kotlin.coroutines.di.module.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CoroutinesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CoroutinesApp)
            modules(listOf(appModule, viewModelModule))
        }
    }
}
