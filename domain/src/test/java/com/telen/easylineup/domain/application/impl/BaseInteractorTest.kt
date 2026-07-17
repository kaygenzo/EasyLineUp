/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Interactors resolve their UseCases/Repositories through Koin (`by inject()`), so
 * exercising them in a unit test requires a running Koin container.
 * Subclasses load their own mocks with `loadKoinModules(module { single<X> { mockX } ... })`
 * in their own `@Before`, which JUnit runs after this one.
 */
internal abstract class BaseInteractorTest {

    @Before
    fun startTestKoin() {
        startKoin {
            modules(
                module {
                    single<Scheduler> { Schedulers.trampoline() }
                }
            )
        }
    }

    @After
    fun stopTestKoin() {
        stopKoin()
    }
}
