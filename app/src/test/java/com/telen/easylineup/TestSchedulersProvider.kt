/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import com.telen.easylineup.domain.usecases.SchedulersProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

private class TestSchedulersProvider : SchedulersProvider {
    override fun main(): Scheduler = Schedulers.trampoline()
    override fun io(): Scheduler = Schedulers.trampoline()
    override fun computation(): Scheduler = Schedulers.trampoline()
}

/** A [SchedulersProvider] that runs everything synchronously on the calling thread. */
fun testSchedulersProvider(): SchedulersProvider = TestSchedulersProvider()
