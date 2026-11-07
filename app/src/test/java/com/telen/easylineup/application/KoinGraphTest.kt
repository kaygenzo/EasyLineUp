/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Every constructor dependency in the app must have a Koin definition. A forgotten one compiles
 * fine and only crashes when its screen opens, which is what this catches on the JVM.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinGraphTest {
    @Test
    fun everyDefinitionFindsItsDependencies() {
        module { includes(ModuleProvider.modules) }.verify(
            extraTypes = listOf(Context::class, Application::class, SavedStateHandle::class)
        )
    }
}
