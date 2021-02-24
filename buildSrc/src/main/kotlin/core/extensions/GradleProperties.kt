import org.gradle.api.Project

fun Project.isLocalDependencies(): Boolean {
    return findTypedProperty("build.localDependencies")
}