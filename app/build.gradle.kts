plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    //kotlin("kapt") version "2.42"
}

android {
    namespace = "com.dev.nastv"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.dev.nastv"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

//    implementation("androidx.core:core-ktx:1.9.0")
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("com.google.android.material:material:1.12.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.core:core-ktx:1.7.0")
            implementation("androidx.appcompat:appcompat:1.5.1")
            implementation("com.google.android.material:material:1.7.0")
            implementation("androidx.legacy:legacy-support-v4:1.0.0")
            implementation("androidx.constraintlayout:constraintlayout:2.1.4")
            implementation("junit:junit:4.13.2")
            androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-dash:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation("androidx.leanback:leanback:1.0.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation("com.google.dagger:hilt-android:2.42")
    kapt("com.google.dagger:hilt-android-compiler:2.42")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")


    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")


    implementation ("com.github.SMehranB:AnimatedTextView:1.1.0")

    implementation ("nl.dionsegijn:konfetti-xml:2.0.4")

   // implementation ( "com.hanks:htextview-base:0.1.6")        // base library
//
//    implementation ( "com.hanks:htextview-fade:0.1.6")        // optional
//    implementation ( "com.hanks:htextview-line:0.1.6" )       // optional
//    implementation ( "com.hanks:htextview-rainbow:0.1.6" )    // optional
//            implementation ( "com.hanks:htextview-typer:0.1.6")       // optional

//    compile "com.hanks:htextview-scale:$htextview_version"       // optional
//    compile "com.hanks:htextview-evaporate:$htextview_version"   // optional
//    compile "com.hanks:htextview-fall:$htextview_version"        // optional

}