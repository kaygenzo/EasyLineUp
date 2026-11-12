# R8 rules for the test APK of the `minified` build type (see app/build.gradle). R8 only has to
# rewrite the test code's references to the app with the app's own mapping: nothing in the test
# APK itself is removed or optimized, and JUnit still finds its annotations by reflection.
-dontshrink
-dontoptimize
-keepattributes *
-dontwarn **
