/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.system

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents.getIntents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.openFixtureLineup
import com.telen.easylineup.testing.string
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The share button hands a PDF of the lineup to the system's share sheet. The share sheet is
 * replaced by espresso-intents, so this checks what the app sends to it, down to the PDF being
 * readable through the granted URI.
 */
@RunWith(AndroidJUnit4::class)
class ShareLineupTest {
    @get:Rule(order = 0)
    val seedRule = SeedDatabaseRule()

    @get:Rule(order = 1)
    val intentsRule = IntentsRule()

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun stubTheShareSheet() {
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun sharingSendsTheLineupPdfThroughTheShareSheet() {
        composeTestRule.openFixtureLineup()

        composeTestRule.onNodeWithContentDescription(string(R.string.action_share)).performClick()
        composeTestRule.waitUntil(10_000) {
            getIntents().any { it.action == Intent.ACTION_CHOOSER }
        }

        intended(hasAction(Intent.ACTION_CHOOSER))
        val chooser = getIntents().last { it.action == Intent.ACTION_CHOOSER }
        @Suppress("DEPRECATION")
        val share = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("application/pdf", share.type)
        assertEquals(
            string(R.string.share_lineup_subject, TestFixtures.LINEUP_NAME),
            share.getStringExtra(Intent.EXTRA_SUBJECT)
        )
        assertTrue(share.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        @Suppress("DEPRECATION")
        val pdf = share.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        context.contentResolver.openFileDescriptor(pdf, "r")!!.use { descriptor ->
            PdfRenderer(descriptor).use { assertEquals(1, it.pageCount) }
        }
    }
}
