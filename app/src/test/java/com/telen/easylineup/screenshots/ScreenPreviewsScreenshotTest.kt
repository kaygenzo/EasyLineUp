/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.screenshots

import android.app.Application
import android.view.ContextThemeWrapper
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.telen.easylineup.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Renders every full-screen `@DugoutPreviews` preview on the JVM and compares it to the reference
 * image in `src/test/screenshots`, in three configurations. New screens are picked up on their
 * own, as long as their preview function is named like `XScreenPreview` or `XScreenEmptyPreview`.
 *
 * Plain unit test runs skip the rendering. After an intended visual change:
 * ```
 * ./gradlew recordRoborazziProdDebug   # regenerate the reference images
 * ./gradlew verifyRoborazziProdDebug   # fail on any difference
 * ```
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class ScreenPreviewsScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
    private val variant: Variant,
    @Suppress("unused") private val name: String
) {
    enum class Variant(val qualifiers: String) {
        SMALL_LIGHT("w360dp-h640dp-port-notnight-xhdpi"),
        SMALL_DARK("w360dp-h640dp-port-night-xhdpi"),
        LARGE_LANDSCAPE_LIGHT("w1280dp-h800dp-land-notnight-mdpi")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun matchesTheReferenceImage() {
        RuntimeEnvironment.setQualifiers(variant.qualifiers)

        captureRoboImage(
            filePath = "src/test/screenshots/${preview.methodName}_${variant.name}.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = CHANGE_THRESHOLD)
            )
        ) {
            val themed = ContextThemeWrapper(LocalContext.current, R.style.AppTheme)
            CompositionLocalProvider(LocalContext provides themed) {
                preview()
            }
        }
    }

    companion object {
        /** Anti-aliasing varies slightly between machines; a real layout change is far above. */
        private const val CHANGE_THRESHOLD = 0.01f
        private val SCREEN_PREVIEW = Regex(".+Screen(Empty|Create|Edit|Editable|Fixed)?Preview")

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
        fun parameters(): List<Array<Any>> {
            val screens = AndroidComposablePreviewScanner()
                .scanPackageTrees("com.telen.easylineup")
                .includePrivatePreviews()
                .getPreviews()
                .filter { SCREEN_PREVIEW.matches(it.methodName) }
                .distinctBy { it.methodName }
                .sortedBy { it.methodName }
            return screens.flatMap { preview ->
                Variant.entries.map { arrayOf(preview, it, "${preview.methodName}_${it.name}") }
            }
        }
    }
}
