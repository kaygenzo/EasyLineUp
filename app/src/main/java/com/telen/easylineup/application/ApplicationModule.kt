/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.utils.SharedPreferencesHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.dsl.module

val appModules = module {
    single { AndroidSchedulers.mainThread() }
    single { SharedPreferencesHelper(get()) }
}
