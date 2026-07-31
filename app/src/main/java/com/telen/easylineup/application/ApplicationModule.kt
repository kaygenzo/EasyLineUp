/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.GeocodingProvider
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.utils.DispatcherProviderImpl
import com.telen.easylineup.utils.GeocodingProviderImpl
import com.telen.easylineup.utils.PhoneNumberValidatorImpl
import com.telen.easylineup.utils.SharedPreferencesHelper
import com.telen.easylineup.utils.StringResourcesProviderImpl
import org.koin.dsl.module

val appModules = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }
    single<StringResourcesProvider> { StringResourcesProviderImpl(get()) }
    single<GeocodingProvider> { GeocodingProviderImpl(get()) }
    single<PhoneNumberValidator> { PhoneNumberValidatorImpl() }
    single { SharedPreferencesHelper(get()) }
}
