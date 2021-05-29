plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("android.extensions")
}

dependencies {
    repositories {
        google()
        mavenCentral()
    }
    implementation(kotlin("stdlib"))
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    implementation("io.ktor:ktor-client-android:1.6.0")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        applicationId = "io.github.durun.pinner"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles.apply {
                add(getDefaultProguardFile("proguard-android-optimize.txt"))
                add(file("proguard-rules.pro"))
            }
        }
    }
    packagingOptions {
        exclude("META-INF/ktor-http.kotlin_module")
        exclude("META-INF/kotlinx-io.kotlin_module")
        exclude("META-INF/atomicfu.kotlin_module")
        exclude("META-INF/ktor-utils.kotlin_module")
        exclude("META-INF/kotlinx-coroutines-io.kotlin_module")
        exclude("META-INF/ktor-client-core.kotlin_module")
        exclude("META-INF/kotlinx-serialization-runtime.kotlin_module")
    }
}