package core.commons

import BuildAndroidConfig
import BuildProductDimensions
import ProductFlavorDevelop
import ProductFlavorProduction
import ProductFlavorQA
import core.dependencies.Dependencies
import core.dependencies.AnnotationProcessorsDependencies
import core.extensions.kapt

plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(BuildAndroidConfig.TARGET_SDK_VERSION)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    android.dataBinding.isEnabled = true

    flavorDimensions(BuildProductDimensions.ENVIRONMENT)
    productFlavors {
        ProductFlavorDevelop.libraryCreate(this)
        ProductFlavorQA.libraryCreate(this)
        ProductFlavorProduction.libraryCreate(this)
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
        getByName("test") {
            java.srcDir("src/test/kotlin")
        }
        getByName("androidTest") {
            java.srcDir("src/androidTest/kotlin")
        }
    }

    lintOptions {
        lintConfig = rootProject.file(".lint/config.xml")
        isCheckAllWarnings = true
        isWarningsAsErrors = true
    }

    sourceSets.getByName("main") {
        res.srcDir("src/main/res/")
        res.srcDir("src/main/res/layouts/activity")
        res.srcDir("src/main/res/layouts/fragment")
        res.srcDir("src/main/res/layouts/views")
    }
}

val androidLibs = listOf(
    Dependencies.APPCOMPAT,
    Dependencies.COROUTINES,
    Dependencies.NAVIGATION_FRAGMENT,
    Dependencies.NAVIGATION_UI,
    Dependencies.LIFECYCLE_VIEWMODEL,
    Dependencies.LIFECYCLE_LIVEDATA,
    Dependencies.CORE_KTX,
    Dependencies.ACTIVITY_KTX,
    Dependencies.FRAGMENT_KTX,
    Dependencies.CONSTRAINT_LAYOUT,
    Dependencies.DAGGER
)

dependencies {
    implementation(project(BuildModules.APP))
    implementation(project(BuildModules.CORE))
    implementation(project(BuildModules.Commons.UI))

    androidLibs.forEach { lib -> implementation(lib) }

    kapt(AnnotationProcessorsDependencies.DAGGER)
    kapt(AnnotationProcessorsDependencies.ROOM)
}
