package com.telen.easylineup.application

import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import bugbattle.io.bugbattle.BugBattle
import bugbattle.io.bugbattle.controller.BugBattleActivationMethod
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.repository.data.AppDatabase
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App: MultiDexApplication() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "easylineup_database")
                .addMigrations(migration_1_2())
                .addMigrations(migration_2_3())
                .addMigrations(migration_3_4())
                .build()

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
            modules(appModules)
        }

        //Stetho.initializeWithDefaults(this)
    }

    private fun migration_1_2(): Migration {
        return object: Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Players ADD COLUMN positions INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration_2_3(): Migration {
        return object: Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Lineups ADD COLUMN mode INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Teams ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration_3_4(): Migration {
        return object: Migration(3,4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Teams ADD COLUMN main INTEGER NOT NULL DEFAULT 1")
            }
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