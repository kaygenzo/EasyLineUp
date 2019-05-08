package com.telen.easylineup

import android.app.Application
import androidx.room.Room
import com.crashlytics.android.Crashlytics
import com.telen.easylineup.data.AppDatabase
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class App: Application() {

     companion object {
         lateinit var database: AppDatabase
     }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "easylineup_database")
                .build()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Fabric.with(this, Crashlytics())
    }
}