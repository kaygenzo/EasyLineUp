package com.telen.easylineup

import androidx.test.runner.AndroidJUnitRunner
import com.squareup.rx2.idler.Rx2Idler
import io.reactivex.plugins.RxJavaPlugins

class EasylineupAndroidTestRunner: AndroidJUnitRunner() {
    override fun onStart() {
        super.onStart()
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("RxJava 2.x Computation Scheduler"))
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"))
        RxJavaPlugins.setInitSingleSchedulerHandler(Rx2Idler.create("RxJava 2.x single Scheduler"))
        RxJavaPlugins.setInitNewThreadSchedulerHandler(Rx2Idler.create("RxJava 2.x new thread Scheduler"))
    }
}