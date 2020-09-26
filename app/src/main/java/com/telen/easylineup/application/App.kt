package com.telen.easylineup.application

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.shakebugs.shake.Shake
import com.telen.easylineup.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        else {
            Timber.plant(ReleaseTree())
        }

        val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        Shake.start(this)

        startKoin {
            androidContext(this@App)
            modules(ModuleProvider.modules)
        }
    }

    class ReleaseTree: Timber.DebugTree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
            when(priority) {
                Log.ERROR -> {
                    t?.let { crashlytics.recordException(t) }
                }
                Log.WARN -> {
                    crashlytics.log("W/${tag ?: "TAG"}: $message")
                }
                else -> {}
            }

        }
    }
}