import core.dependencies.Dependencies
import core.extensions.implementation

plugins {
    plugin(BuildPlugins.commonsAndroidLibrary)
}

val androidLibs = listOf(
    Dependencies.CONSTRAINT_LAYOUT,
    Dependencies.NAVIGATION_FRAGMENT,
    Dependencies.NAVIGATION_UI,
    Dependencies.FRAGMENT_KTX
)

dependencies {
    implementation(project(BuildModules.Commons.UI))
    implementation(Dependencies.MerseyLibs.utils)

    androidLibs.forEach { lib -> implementation(lib) }
}
