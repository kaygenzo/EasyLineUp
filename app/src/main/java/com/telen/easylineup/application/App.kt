package com.telen.easylineup.application

import android.util.Log
import androidx.multidex.MultiDexApplication
import bugbattle.io.bugbattle.BugBattle
import bugbattle.io.bugbattle.controller.BugBattleActivationMethod
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.telen.easylineup.BuildConfig
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

//        StrictMode.setVmPolicy(VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .penaltyDeath()
//                .build())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        else {
            Timber.plant(ReleaseTree())
        }

        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(this, crashlytics)

        BugBattle.initialise(BuildConfig.ReportToolApiKey, BugBattleActivationMethod.SHAKE, this)

        startKoin {
            androidContext(this@App)
            modules(ModuleProvider.modules)
        }
    }

    class ReleaseTree: Timber.DebugTree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            when(priority) {
                Log.ERROR -> {
                    Crashlytics.logException(t)
                }
                Log.WARN -> {
                    Crashlytics.log(priority, tag, message)
                }
                else -> {}
            }

        }
    }
}