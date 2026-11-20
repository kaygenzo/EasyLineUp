/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** The smallest screen width the app's layouts were drawn for (a Pixel 9a): scale 1 there. */
private const val REFERENCE_SMALLEST_WIDTH_DP = 411f

/** A 360 dp wide phone gets 0.88: a bit smaller, still above the 48 dp touch targets' limit. */
private const val MIN_SCALE = 0.88f

/** Reached from 535 dp: a tablet's text and rows grow a third, not in proportion to its size. */
private const val MAX_SCALE = 1.3f

/**
 * How much bigger (or smaller) [content] draws on this screen than on the reference phone: the
 * screen's smallest width over the reference one's, kept between [MIN_SCALE] and [MAX_SCALE].
 * The smallest width doesn't change with the orientation, so a rotation keeps the same sizes.
 */
internal fun adaptiveScale(smallestScreenWidthDp: Int): Float =
    (smallestScreenWidthDp / REFERENCE_SMALLEST_WIDTH_DP).coerceIn(MIN_SCALE, MAX_SCALE)

/**
 * Scales every dp and sp in [content] to the screen: text, icons, paddings and rows grow on a
 * tablet, where they looked lost at their phone size, and shrink a little on a small phone,
 * where they crowded each other. Layouts keep their proportions, since everything scales
 * together. Sheets and dialogs opened from [content] are windows of their own and keep their
 * size.
 */
@Composable
fun AdaptiveScale(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scale = adaptiveScale(LocalConfiguration.current.smallestScreenWidthDp)
    CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale),
        content = content
    )
}

@DugoutPreviews
@Composable
private fun AdaptiveScalePreview() {
    DugoutTheme {
        AdaptiveScale {
            Text("Scaled to the screen")
        }
    }
}
