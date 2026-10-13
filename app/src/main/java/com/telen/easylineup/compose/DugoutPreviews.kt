/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview annotation every Dugout Card composable screen should carry: small and large
 * screen, portrait and landscape. Apply to a private `@Composable *Preview()` function that
 * wraps the real screen in [DugoutTheme] with representative/empty state.
 */
@Preview(name = "Small portrait", device = "spec:width=360dp,height=640dp")
@Preview(name = "Small landscape", device = "spec:width=640dp,height=360dp")
@Preview(name = "Large portrait", device = "spec:width=800dp,height=1280dp")
@Preview(name = "Large landscape", device = "spec:width=1280dp,height=800dp")
annotation class DugoutPreviews
