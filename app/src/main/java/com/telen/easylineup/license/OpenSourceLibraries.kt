/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.license

/** A license, by its own untranslated name. */
enum class OpenSourceLicense(val displayName: String) {
    APACHE2("Apache License 2.0"),
}

/**
 * @property name the library's name as its project spells it
 * @property url the project's home page
 */
data class OpenSourceLibrary(val name: String, val url: String, val license: OpenSourceLicense)

/** The open source libraries shipped inside the app, as the Settings licenses dialog lists them. */
val openSourceLibraries: List<OpenSourceLibrary> = listOf(
    OpenSourceLibrary(
        "AndroidX Activity",
        "https://developer.android.com/jetpack/androidx/releases/activity",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Annotation",
        "https://developer.android.com/jetpack/androidx/releases/annotation",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Core",
        "https://developer.android.com/jetpack/androidx/releases/core",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX DocumentFile",
        "https://developer.android.com/jetpack/androidx/releases/documentfile",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Lifecycle",
        "https://developer.android.com/jetpack/androidx/releases/lifecycle",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Navigation",
        "https://developer.android.com/jetpack/androidx/releases/navigation",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Preference",
        "https://developer.android.com/jetpack/androidx/releases/preference",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Room",
        "https://developer.android.com/jetpack/androidx/releases/room",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "AndroidX Core Splashscreen",
        "https://developer.android.com/jetpack/androidx/releases/core",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "Jetpack Compose",
        "https://developer.android.com/jetpack/androidx/releases/compose",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "Material Components for Android",
        "https://github.com/material-components/material-components-android",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary("Kotlin", "https://github.com/JetBrains/kotlin", OpenSourceLicense.APACHE2),
    OpenSourceLibrary(
        "kotlinx.coroutines",
        "https://github.com/Kotlin/kotlinx.coroutines",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "kotlinx.serialization",
        "https://github.com/Kotlin/kotlinx.serialization",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "kotlinx.collections.immutable",
        "https://github.com/Kotlin/kotlinx.collections.immutable",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "Firebase Android SDK",
        "https://github.com/firebase/firebase-android-sdk",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary("Gson", "https://github.com/google/gson", OpenSourceLicense.APACHE2),
    OpenSourceLibrary("Koin", "https://github.com/InsertKoinIO/koin", OpenSourceLicense.APACHE2),
    OpenSourceLibrary("Coil", "https://github.com/coil-kt/coil", OpenSourceLicense.APACHE2),
    OpenSourceLibrary(
        "Timber",
        "https://github.com/JakeWharton/timber",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "ColorPickerPreference",
        "https://github.com/skydoves/ColorPickerPreference",
        OpenSourceLicense.APACHE2
    ),
    OpenSourceLibrary(
        "ColorPickerView",
        "https://github.com/skydoves/ColorPickerView",
        OpenSourceLicense.APACHE2
    ),
)
