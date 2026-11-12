# Extra R8 rules for the `minified` build type only (see app/build.gradle), never for release.
#
# The test APK does not bundle the libraries the app already ships: it calls the app's copy. R8
# strips or renames whatever the app itself does not use, so these keep the libraries the tests
# call into whole. None of them touches the app's own classes - the export classes Gson reads
# by reflection go through exactly the release rules, which is what the minified tests check.
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class org.koin.** { *; }
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }
-keep class org.junit.** { *; }
-keep class junit.** { *; }
-keep class org.hamcrest.** { *; }

# The tests seed and read the database through the domain ports and use cases, including
# methods the app itself never calls. Kept whole for the same reason; `export.json`, the
# package Gson reflects on, is deliberately not in this list.
-keep class com.telen.easylineup.domain.** { *; }
-keep class com.telen.easylineup.repository.** { *; }

# The one function the export tests call to write a file, which R8 would otherwise inline into
# its only caller. It holds no field Gson reads.
-keep class com.telen.easylineup.export.json.ExportGsonKt { *; }
