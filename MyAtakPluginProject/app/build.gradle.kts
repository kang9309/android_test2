plugins {
//    alias(libs.plugins.android.application)
    id("com.android.library") // 이렇게 변경합니다.
}

android {
    namespace = "com.atakmap.android.myatakpluginproject"
    compileSdk = 36

    defaultConfig {
//        applicationId = "com.atakmap.android.myatakpluginproject"
        minSdk = 24
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}