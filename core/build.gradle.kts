import core.dependencies.Dependencies
import core.dependencies.AnnotationProcessorsDependencies
import core.extensions.implementation
import core.extensions.kapt
import core.isLocalDependencies

plugins {
    plugin(BuildPlugins.commonsAndroidLibrary)
}

val androidLibs = listOf(
    Dependencies.ROOM,
    Dependencies.ROOM_KTX,
    Dependencies.NAVIGATION_UI,
    Dependencies.FRAGMENT_KTX,
    Dependencies.CORE_KTX,
    Dependencies.RETROFIT,
    Dependencies.RETROFIT_CONVERTER,
    Dependencies.LOGGING,
    Dependencies.PAGING,
    Dependencies.TYPED_DATASTORE
)

val merseyModules = listOf(
    BuildModules.Libraries.MerseyLibs.archy,
    BuildModules.Libraries.MerseyLibs.utils
)

val merseyLibs = listOf(
    Dependencies.MerseyLibs.archy,
    Dependencies.MerseyLibs.utils
)

dependencies {

    if (isLocalDependencies()) {
        merseyModules.forEach { module -> api(project(module)) }
    } else {
        merseyLibs.forEach { lib -> api(lib) }
    }

    androidLibs.forEach { lib -> implementation(lib) }

    kapt(AnnotationProcessorsDependencies.ROOM)
}
