/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.utils.UserPreferencesStore
import com.telen.easylineup.utils.applyAsNightMode
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            crashlytics.setCustomKey("installer", installerPackageName() ?: "unknown")
            Timber.plant(ReleaseTree(crashlytics))
        }

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(appCheckProviderFactory())

        val koinApp = startKoin {
            androidContext(this@App)
            modules(ModuleProvider.modules)
        }

        // Before the first Activity is created, so it starts in the right theme.
        koinApp.koin.get<UserPreferencesStore>().read().themeMode.applyAsNightMode()
    }

    /**
     * Play Integrity only vouches for an app installed from the Play Store: debug builds swap in
     * the debug provider (see DebugApp).
     */
    protected open fun appCheckProviderFactory(): AppCheckProviderFactory =
        PlayIntegrityAppCheckProviderFactory.getInstance()

    private fun installerPackageName(): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)
        }
    }.getOrNull()

    /**
     * Sends errors with an exception to Crashlytics as non-fatal reports, and other warnings and
     * errors as breadcrumbs; everything below a warning is dropped before Timber even formats the
     * message.
     */
    class ReleaseTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {
        override fun isLoggable(tag: String?, priority: Int) = priority >= Log.WARN

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            when (priority) {
                Log.ERROR, Log.ASSERT -> t?.let(crashlytics::recordException)
                    ?: crashlytics.log("E/${tag ?: ""}: $message")
                else -> crashlytics.log("W/${tag ?: ""}: $message")
            }
        }
    }
}
