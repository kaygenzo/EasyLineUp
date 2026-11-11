/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A player or team photo goes through Coil into a bitmap the avatar draws: this decodes a real
 * image file, at the avatar's size in pixels.
 */
@RunWith(AndroidJUnit4::class)
class DugoutAvatarBitmapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aPhotoFileIsDecodedAtTheAvatarSize() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "avatar-test.png")
        file.outputStream().use { out ->
            Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
                .apply { eraseColor(Color.RED) }
                .compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        var bitmap: ImageBitmap? = null
        var expectedPx = 0

        composeRule.setContent {
            expectedPx = with(LocalDensity.current) { 56.dp.roundToPx() }
            bitmap = rememberDugoutAvatarBitmap(Uri.fromFile(file), 56.dp)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { bitmap != null }
        // Scaled to fill the square avatar: the shorter side matches it.
        assertEquals(expectedPx, minOf(bitmap!!.width, bitmap!!.height))
        file.delete()
    }
}
