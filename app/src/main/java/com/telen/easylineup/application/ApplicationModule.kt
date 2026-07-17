/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.domain.usecases.SchedulersProvider
import com.telen.easylineup.utils.SchedulersProviderImpl
import com.telen.easylineup.utils.SharedPreferencesHelper
import org.koin.dsl.module

val appModules = module {
    single<SchedulersProvider> { SchedulersProviderImpl() }
    single { SharedPreferencesHelper(get()) }
}
