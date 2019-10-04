package com.telen.easylineup

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import bugbattle.io.bugbattle.BugBattle
import bugbattle.io.bugbattle.controller.BugBattleActivationMethod
import com.crashlytics.android.Crashlytics
import com.telen.easylineup.data.AppDatabase
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class App: MultiDexApplication() {

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "easylineup_database")
                .addMigrations(migration_1_2())
                .build()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Fabric.with(this, Crashlytics())

        BugBattle.initialise(BuildConfig.ReportToolApiKey, BugBattleActivationMethod.SHAKE, this)

        //Stetho.initializeWithDefaults(this)
    }

    private fun migration_1_2(): Migration {
        return object: Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Players ADD COLUMN positions INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}