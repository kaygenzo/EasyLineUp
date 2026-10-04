/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.compose

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.AttrRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

/**
 * Bridges the app's existing XML Material3 theme (AppTheme in themes.xml /
 * values-night/themes.xml, backed by material-colors.xml) into a Compose
 * ColorScheme, so new Compose screens render with the exact same "Dugout
 * Card" palette as the rest of the app instead of duplicating color values.
 *
 * Roles with no dedicated XML token (outlineVariant, the surfaceContainer*
 * tones, scrim, surfaceTint, surface dim/bright) fall back to Compose's
 * baseline M3 defaults, same as the rest of the app, which doesn't define
 * them either.
 */
@Composable
fun DugoutTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // Re-read whenever the configuration (day/night) changes so recreating
    // the ComposeView after a theme flip picks up the new palette.
    val uiMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val colorScheme = remember(uiMode) { context.toDugoutColorScheme() }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

private fun Context.toDugoutColorScheme(): ColorScheme {
    val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    fun attr(@AttrRes attrRes: Int): Color =
        Color(MaterialColors.getColor(this, attrRes, "DugoutTheme: unresolved attr"))

    return base.copy(
        primary = attr(AppCompatR.attr.colorPrimary),
        onPrimary = attr(MaterialR.attr.colorOnPrimary),
        primaryContainer = attr(MaterialR.attr.colorPrimaryContainer),
        onPrimaryContainer = attr(MaterialR.attr.colorOnPrimaryContainer),
        inversePrimary = attr(MaterialR.attr.colorPrimaryInverse),
        secondary = attr(MaterialR.attr.colorSecondary),
        onSecondary = attr(MaterialR.attr.colorOnSecondary),
        secondaryContainer = attr(MaterialR.attr.colorSecondaryContainer),
        onSecondaryContainer = attr(MaterialR.attr.colorOnSecondaryContainer),
        tertiary = attr(MaterialR.attr.colorTertiary),
        onTertiary = attr(MaterialR.attr.colorOnTertiary),
        tertiaryContainer = attr(MaterialR.attr.colorTertiaryContainer),
        onTertiaryContainer = attr(MaterialR.attr.colorOnTertiaryContainer),
        error = attr(AppCompatR.attr.colorError),
        onError = attr(MaterialR.attr.colorOnError),
        errorContainer = attr(MaterialR.attr.colorErrorContainer),
        onErrorContainer = attr(MaterialR.attr.colorOnErrorContainer),
        background = attr(android.R.attr.colorBackground),
        onBackground = attr(MaterialR.attr.colorOnBackground),
        surface = attr(MaterialR.attr.colorSurface),
        onSurface = attr(MaterialR.attr.colorOnSurface),
        surfaceVariant = attr(MaterialR.attr.colorSurfaceVariant),
        onSurfaceVariant = attr(MaterialR.attr.colorOnSurfaceVariant),
        outline = attr(MaterialR.attr.colorOutline),
        inverseSurface = attr(MaterialR.attr.colorSurfaceInverse),
        inverseOnSurface = attr(MaterialR.attr.colorOnSurfaceInverse),
        surfaceTint = attr(AppCompatR.attr.colorPrimary)
    )
}
