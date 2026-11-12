/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import timber.log.Timber

class ReleaseTreeTest {
    private val crashlytics: FirebaseCrashlytics = mock(FirebaseCrashlytics::class.java)
    private val tree = App.ReleaseTree(crashlytics)

    @Before
    fun plant() {
        Timber.plant(tree)
    }

    @After
    fun uproot() {
        Timber.uprootAll()
    }

    @Test
    fun anErrorWithAnExceptionIsReported() {
        val error = IllegalStateException("boom")

        Timber.e(error, "while saving")

        verify(crashlytics).recordException(error)
    }

    @Test
    fun anErrorWithoutAnExceptionIsABreadcrumb() {
        Timber.tag("Import").e("no file")

        verify(crashlytics).log("E/Import: no file")
    }

    @Test
    fun aWarningIsABreadcrumb() {
        Timber.tag("Sync").w("slow")

        verify(crashlytics).log("W/Sync: slow")
    }

    @Test
    fun debugLogsNeverReachCrashlytics() {
        Timber.d("noise")
        Timber.i("noise")
        Timber.v("noise")

        verifyNoInteractions(crashlytics)
    }
}
