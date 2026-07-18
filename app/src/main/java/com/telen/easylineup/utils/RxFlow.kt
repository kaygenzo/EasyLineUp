/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.reactive.asFlow
import timber.log.Timber

fun <T : Any> Flowable<T>.asSafeFlow(): Flow<T> = this.asFlow().catch { Timber.e(it) }
