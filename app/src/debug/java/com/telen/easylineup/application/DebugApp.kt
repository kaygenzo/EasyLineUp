/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.os.StrictMode

class DebugApp : App() {
    override fun onCreate() {
        // Debug builds only, and logged rather than fatal: the point is to see disk or network
        // work on the main thread and leaked closeables in logcat while developing, not to crash
        // on a third-party library doing it.
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
        )
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        super.onCreate()
    }
}
