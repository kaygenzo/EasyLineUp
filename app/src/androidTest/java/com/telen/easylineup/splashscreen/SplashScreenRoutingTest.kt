/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.testing.ActivityTestUtils
import com.telen.easylineup.testing.DatabaseSeeder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Where the app lands on launch: the screen every session starts from. */
@RunWith(AndroidJUnit4::class)
class SplashScreenRoutingTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun closeLandingScreen() {
        ActivityTestUtils.finishAll()
    }

    @Test
    fun goesToHomeWhenATeamExists() {
        runBlocking { DatabaseSeeder().seedCoreFixture() }

        ActivityTestUtils.launch(context, SplashScreenActivity::class.java)

        ActivityTestUtils.waitForResumed(HomeActivity::class.java)
    }

    @Test
    fun goesToLoginWhenThereIsNoTeam() {
        runBlocking { DatabaseSeeder().clear() }

        ActivityTestUtils.launch(context, SplashScreenActivity::class.java)

        ActivityTestUtils.waitForResumed(LoginActivity::class.java)
    }

    @Test
    fun goesToImportWhenOpenedWithAFile() {
        runBlocking { DatabaseSeeder().seedCoreFixture() }
        val file = File(context.cacheDir, "splash.elu").apply { writeText("""{"teams":[]}""") }

        ActivityTestUtils.launch(context, SplashScreenActivity::class.java) {
            data = Uri.fromFile(file)
        }

        ActivityTestUtils.waitForResumed(ImportDataActivity::class.java)
    }
}
