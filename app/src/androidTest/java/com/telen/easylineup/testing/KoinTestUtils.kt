/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.testing

import org.koin.core.context.GlobalContext

/**
 * Resolves [T] lazily from the Koin graph the app under test started. Tests use this rather
 * than implementing `KoinComponent`: the app itself no longer does, so on the `minified` build
 * (minSdk below 24) R8 drops the class that carries `KoinComponent`'s default methods and a
 * test implementing the interface fails to load.
 */
inline fun <reified T : Any> inject(): Lazy<T> = lazy { GlobalContext.get().get<T>() }
