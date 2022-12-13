val appVersionCode: Int by rootProject.extra
val appVersionName: String by rootProject.extra

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.application)
    kotlin("android") // alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    kotlin("plugin.serialization") // alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.secrets) // Secrets Gradle Plugin for Android: https://github.com/google/secrets-gradle-plugin
}

android {
    namespace = "com.bqliang.leavesheet"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.bqliang.leavesheet"
        minSdk = 26
        targetSdk = 33
        versionCode = appVersionCode
        versionName = appVersionName

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    signingConfigs {
        signingConfigs.create("release") {
            storeFile = file("../key.jks")
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName(name)
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "${rootProject.name}-${versionName}-${name/* variant name */}.apk"
        }
    }

    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    sourceSets {
        getByName("main") {
            res {
                val srcDirs = listOf(
                    "main",
                    "settings",
                    "scan"
                ).map { "src/main/java/com/bqliang/leavesheet/$it/res" }.plus("src/main/res")
                setSrcDirs(srcDirs)
            }
        }
    }
}

dependencies {
    implementation(libs.guolin.permissionX)
    implementation(libs.huawei.scanPlus)
    implementation(libs.tencent.mmkv)
    implementation(libs.timber)
    implementation(libs.rikkaX.preference.simplemenu)
    implementation(libs.bundles.rikkaX.html)
    implementation(libs.kotlinX.serialization.json)
    implementation(libs.kotlinX.coroutines.android)
    implementation(libs.bundles.appCenter)
    implementation(libs.xabaras.recyclerViewSwipeDecorator)
    implementation(libs.faruktoptas.fancyShowCaseView)
    implementation(libs.google.material)
    implementation(libs.androidX.core)
    implementation(libs.androidX.appCompat)
    implementation(libs.androidX.activity)
    implementation(libs.androidX.fragment)
    implementation(libs.bundles.androidX.lifeCycle)
    implementation(libs.androidX.constraintLayout)
    implementation(libs.androidX.preference)
    implementation(libs.androidX.splashScreen)
    implementation(libs.androidX.swipeRefreshLayout)
    implementation(libs.bundles.androidX.dataStore)
    implementation(libs.bundles.androidX.room)
    kapt(libs.androidX.room.compiler)
}