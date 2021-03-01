/**
 * Configuration of all gradle build plugins
 */
object BuildPlugins {
    val androidApplication = GradlePlugin(id = "com.android.application")
    val androidLibrary = GradlePlugin(id = "com.android.library")
    val commonsAndroidLibrary = GradlePlugin(id = "core.commons.android-library")
    val kotlinKapt = GradlePlugin(id = "kotlin-kapt")
    val kotlinAndroid = GradlePlugin(id = "kotlin-android")
    val dynamicFeature = GradlePlugin(id = "com.android.dynamic-feature")
    val commonsDynamicFeature = GradlePlugin(id = "core.commons.android-dynamic-feature")
    val updateDependencies = GradlePlugin(id = "plugins.update-dependencies")
    val detekt = GradlePlugin(id = "plugins.detekt")
    val navigationArgs = GradlePlugin(id = "androidx.navigation.safeargs.kotlin")
    val kotlinParcelize = GradlePlugin(id = "kotlin-parcelize")

    val kotlinSerialization = GradlePlugin(
        id = "kotlinx-serialization",
        module = "org.jetbrains.kotlin:kotlin-serialization:${LibraryVersions.Plugins.serialization}"
    )
}
