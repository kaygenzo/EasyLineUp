package com.telen.easylineup.application

import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.dsl.module

val appModules = module {
    single { AndroidSchedulers.mainThread() }
}