/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.telen.easylineup.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SafeIntentsTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:48.85,2.35"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    @Test
    fun anIntentNoAppHandlesShowsAToastInsteadOfCrashing() {
        // Without this, Robolectric pretends every intent resolves.
        shadowOf(context).checkActivities(true)

        val started = context.startActivityOrNotify(geoIntent)

        assertFalse(started)
        assertEquals(
            context.getString(R.string.error_no_app_for_action),
            ShadowToast.getTextOfLatestToast()
        )
    }

    @Test
    fun anIntentAnAppHandlesStartsIt() {
        val started = context.startActivityOrNotify(geoIntent)

        assertTrue(started)
        assertEquals(geoIntent, shadowOf(context).nextStartedActivity)
        assertNull(ShadowToast.getLatestToast())
    }
}
