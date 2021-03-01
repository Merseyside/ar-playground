enableFeaturePreview("GRADLE_METADATA")

include(
    ":app",
    ":core",
    ":commons:ui",
    ":commons:views"
)

private val isLocalDependencies = true

if (isLocalDependencies) {

    include(":utils")
    project(":utils").projectDir =
            File(rootDir.parent, "mersey-android-library/utils")

    include(":animators")
    project(":animators").projectDir =
            File(rootDir.parent, "mersey-android-library/animators")

    include(":archy")
    project(":archy").projectDir =
            File(rootDir.parent, "mersey-android-library/archy")

    include(":adapters")
    project(":adapters").projectDir =
            File(rootDir.parent, "mersey-android-library/adapters")

}

rootProject.name="ar-playground"
rootProject.buildFileName = "build.gradle.kts"