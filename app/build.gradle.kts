plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.neuerordner.main"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neuerordner.main"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}




dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test:core:1.5.0")     // ApplicationProvider
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("com.github.javafaker:javafaker:1.0.2")
    androidTestImplementation ("org.mockito:mockito-core:5.18.0")

// ApplicationProvider
    implementation(project(":hashadapter-library"))
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("com.google.zxing:javase:3.5.2") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation ("androidx.camera:camera-extensions:1.4.2")
    implementation ("com.google.code.gson:gson:2.13.1")
    implementation ("com.google.mlkit:text-recognition:16.0.1")
    implementation ("com.google.android.gms:play-services-ads:24.5.0") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation ("androidx.concurrent:concurrent-futures:1.1.0")



}

