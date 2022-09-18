package com.telen.easylineup.license

import android.content.Context
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import com.telen.easylineup.R

object LicensesManager {
    private val libraries = listOf(
        Library("Timber", "https://github.com/JakeWharton/timber", License.APACHE2),
        Library("RxJava", "https://github.com/ReactiveX/RxJava", License.APACHE2),
        Library("RxAndroid", "https://github.com/ReactiveX/RxAndroid", License.APACHE2),
        Library("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart", License.APACHE2),
        Library("Picasso", "https://github.com/square/picasso", License.APACHE2),
        Library("RoundedImageView", "https://github.com/vinc3m1/RoundedImageView", License.APACHE2),
        Library("Koin", "https://github.com/InsertKoinIO/koin", License.APACHE2),
        Library("TapTargetView", "https://github.com/KeepSafe/TapTargetView", License.APACHE2),
        Library("Dexter", "https://github.com/Karumi/Dexter", License.APACHE2),
        Library("Licenser", "https://github.com/marcoscgdev/Licenser", License.MIT),
        Library("LeakCanary", "https://github.com/square/leakcanary", License.APACHE2),
        // AndroidX
        Library(
            "RecyclerView",
            "https://github.com/androidx-releases/Recyclerview",
            License.APACHE2
        ),
        Library("Preference", "https://github.com/androidx-releases/Preference", License.APACHE2),
        Library(
            "ConstraintLayout",
            "https://github.com/androidx-releases/ConstraintLayout",
            License.APACHE2
        ),
        Library("Appcompat", "https://github.com/androidx-releases/Appcompat", License.APACHE2),
        Library("Lifecycle", "https://github.com/androidx-releases/Lifecycle", License.APACHE2),
        Library("Multidex", "https://github.com/androidx-releases/Multidex", License.APACHE2),
        Library("Navigation", "https://github.com/androidx-releases/Navigation", License.APACHE2),
        Library(
            "DocumentFile",
            "https://github.com/androidx-releases/Documentfile",
            License.APACHE2
        ),
        Library("Annotation", "https://github.com/androidx-releases/Annotation", License.APACHE2),
        Library("Room", "https://github.com/androidx-releases/Room", License.APACHE2),
        // Google
        Library(
            "Android Material Components",
            "https://github.com/material-components/material-components-android",
            License.APACHE2
        ),
        Library("Gson", "https://github.com/google/gson", License.APACHE2),
        Library("Firebase", "https://github.com/firebase/firebase-android-sdk", License.APACHE2),
        // test
        Library("AndroidX Test", "https://github.com/androidx-releases/Test", License.APACHE2),
        Library("Barista", "https://github.com/AdevintaSpain/Barista", License.APACHE2),
        Library("Fastlane", "https://github.com/fastlane/fastlane", License.MIT),
        Library("RxIdler", "https://github.com/square/RxIdler", License.APACHE2),
        Library("Mockito", "https://github.com/mockito/mockito", License.MIT),
        Library("Mockito Kotlin", "https://github.com/mockito/mockito-kotlin", License.MIT),
        Library("Detekt", "https://github.com/detekt/detekt", License.APACHE2),
        Library(
            "DependencyCheck",
            "https://github.com/dependency-check/dependency-check-gradle",
            License.APACHE2
        ),
        Library(
            "Sonar Scanner",
            "https://github.com/SonarSource/sonar-scanner-gradle",
            License.APACHE2
        ),
    )

    fun getDialog(context: Context): LicenserDialog {
        var builder = LicenserDialog(context)
            .setTitle("Licenses")
            .setCustomNoticeTitle(R.string.libraries_title)
        libraries.forEach {
            builder = builder.setLibrary(it)
        }
        builder.setPositiveButton(android.R.string.ok) { dialog, i ->
            dialog.dismiss()
        }
        return builder
    }
}