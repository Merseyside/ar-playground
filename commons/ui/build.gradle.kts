import core.dependencies.Dependencies
import core.dependencies.AnnotationProcessorsDependencies
import core.extensions.implementation
import core.extensions.kapt
import core.isLocalDependencies

plugins {
    plugin(BuildPlugins.commonsAndroidLibrary)
}

val merseyModules = listOf(
    BuildModules.Libraries.MerseyLibs.archy,
    BuildModules.Libraries.MerseyLibs.utils
)

val merseyLibs = listOf(
    Dependencies.MerseyLibs.archy,
    Dependencies.MerseyLibs.utils
)

val androidLibs = listOf(
    Dependencies.LIFECYCLE_VIEWMODEL,
    Dependencies.CONSTRAINT_LAYOUT,
    Dependencies.RECYCLE_VIEW,
    Dependencies.CORE_KTX,
    Dependencies.FRAGMENT_KTX,
    Dependencies.NAVIGATION_FRAGMENT,
    Dependencies.NAVIGATION_UI,
    Dependencies.PAGING
)

dependencies {
    implementation(project(BuildModules.CORE))

    if (isLocalDependencies()) {
        merseyModules.forEach { module -> api(project(module)) }
    } else {
        merseyLibs.forEach { lib -> api(lib) }
    }

    androidLibs.forEach { lib -> implementation(lib) }
}
