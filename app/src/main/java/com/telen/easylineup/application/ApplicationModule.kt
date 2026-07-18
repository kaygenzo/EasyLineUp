/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.domain.usecases.GeocodingPort
import com.telen.easylineup.domain.usecases.PhoneNumberValidator
import com.telen.easylineup.domain.usecases.SchedulersProvider
import com.telen.easylineup.domain.usecases.StringResourcesProvider
import com.telen.easylineup.utils.GeocodingPortImpl
import com.telen.easylineup.utils.PhoneNumberValidatorImpl
import com.telen.easylineup.utils.SchedulersProviderImpl
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.utils.StringResourcesProviderImpl
import org.koin.dsl.module

val appModules = module {
    single<SchedulersProvider> { SchedulersProviderImpl() }
    single<StringResourcesProvider> { StringResourcesProviderImpl(get()) }
    single<GeocodingPort> { GeocodingPortImpl(get()) }
    single<PhoneNumberValidator> { PhoneNumberValidatorImpl() }
    single { SharedPreferencesHelper(get()) }
}
