package com.telen.easylineup.domain.ports

import io.reactivex.rxjava3.core.Scheduler

interface SchedulersProvider {
    fun main(): Scheduler
    fun io(): Scheduler
    fun computation(): Scheduler
}