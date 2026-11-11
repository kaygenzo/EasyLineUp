/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.system

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.splashscreen.SplashScreenActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The manifest's intent filters decide which files Android offers to open with the app. These
 * ask the package manager, as another app would, which activity a given file resolves to.
 */
@RunWith(AndroidJUnit4::class)
class IntentFiltersTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun resolvedActivities(
        uri: String,
        mimeType: String? = null,
        browsable: Boolean = false
    ): List<String> {
        val intent = Intent(Intent.ACTION_VIEW).setPackage(context.packageName)
        if (browsable) {
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
        }
        if (mimeType == null) {
            intent.data = Uri.parse(uri)
        } else {
            intent.setDataAndType(Uri.parse(uri), mimeType)
        }
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.name }
    }

    @Test
    fun theLauncherOpensTheSplashScreen() {
        val launcher = context.packageManager.getLaunchIntentForPackage(context.packageName)

        assertEquals(SplashScreenActivity::class.java.name, launcher?.component?.className)
    }

    @Test
    fun aFileUriIsNotClaimed() {
        assertTrue(resolvedActivities("file://storage/emulated/0/Download/team.elu").isEmpty())
    }

    @Test
    fun anEluFileFromAContentProviderOpensTheSplashScreen() {
        assertEquals(
            listOf(SplashScreenActivity::class.java.name),
            resolvedActivities("content://com.android.providers.downloads.documents/team.elu")
        )
    }

    @Test
    fun aBinaryFileSharedWithoutExtensionOpensTheSplashScreen() {
        assertEquals(
            listOf(SplashScreenActivity::class.java.name),
            resolvedActivities(
                "content://com.android.providers.downloads.documents/document/42",
                "application/octet-stream"
            )
        )
    }

    @Test
    fun otherFilesAreNotClaimed() {
        assertTrue(resolvedActivities("file://storage/emulated/0/Download/team.pdf").isEmpty())
        assertTrue(resolvedActivities("https://example.com/team.elu").isEmpty())
    }

    @Test
    fun aBinaryFileOpenedFromAWebPageIsNotClaimed() {
        val download = "content://com.android.providers.downloads.documents/document/42"

        assertTrue(
            resolvedActivities(download, "application/octet-stream", browsable = true).isEmpty()
        )
    }
}
