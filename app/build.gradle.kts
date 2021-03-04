import core.dependencies.Dependencies
import core.isLocalDependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    plugin(BuildPlugins.androidApplication)
    plugin(BuildPlugins.kotlinAndroid)
    plugin(BuildPlugins.kotlinKapt)
    plugin(BuildPlugins.kotlinSerialization)
    plugin(BuildPlugins.navigationArgs)
}

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)

    dexOptions {
        javaMaxHeapSize = "2g"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = BuildAndroidConfig.APPLICATION_ID
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(BuildAndroidConfig.TARGET_SDK_VERSION)
        buildToolsVersion(BuildAndroidConfig.BUILD_TOOLS_VERSION)

        versionCode = BuildAndroidConfig.VERSION_CODE
        versionName = BuildAndroidConfig.VERSION_NAME

        vectorDrawables.useSupportLibrary = BuildAndroidConfig.SUPPORT_LIBRARY_VECTOR_DRAWABLES

        multiDexEnabled = true
    }

    buildFeatures.dataBinding = true

    flavorDimensions(BuildProductDimensions.ENVIRONMENT)
    productFlavors {
        ProductFlavorDevelop.appCreate(this)
        ProductFlavorQA.appCreate(this)
        ProductFlavorProduction.appCreate(this)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
        }
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }

    sourceSets.getByName("main") {
        res.srcDir("src/main/res/")
        res.srcDir("src/main/res/layouts/fragment")
        res.srcDir("src/main/res/layouts/activity")
        res.srcDir("src/main/res/value/values-light")
        res.srcDir("src/main/res/value/values-night")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val androidLibs = listOf(
    Dependencies.APPCOMPAT,
    Dependencies.MATERIAL,
    Dependencies.NAVIGATION_FRAGMENT,
    Dependencies.FRAGMENT_KTX,
    Dependencies.RECYCLE_VIEW,
    Dependencies.LIFECYCLE_VIEWMODEL,
    Dependencies.LIFECYCLE_RUNTIME,
    Dependencies.CONSTRAINT_LAYOUT,
    Dependencies.DAGGER,
    Dependencies.TYPED_DATASTORE,
    Dependencies.AR_CORE,
    Dependencies.OBJ_LOADER
)

val modulez = listOf(
    BuildModules.CORE
)

val merseyModules = listOf(
    BuildModules.Libraries.MerseyLibs.archy,
    BuildModules.Libraries.MerseyLibs.utils
)

val merseyLibs = listOf(
    Dependencies.MerseyLibs.adapters,
    Dependencies.MerseyLibs.animators,
    Dependencies.MerseyLibs.archy,
    Dependencies.MerseyLibs.utils
)

dependencies {
    modulez.forEach { module -> implementation(project(module)) }

    if (isLocalDependencies()) {
        merseyModules.forEach { module -> api(project(module)) }
    } else {
        merseyLibs.forEach { lib -> api(lib) }
    }

    androidLibs.forEach { lib -> implementation(lib) }
    compileOnly("javax.annotation:jsr250-api:1.0")
}