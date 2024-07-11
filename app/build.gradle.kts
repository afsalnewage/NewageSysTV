plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id("com.google.dagger.hilt.android")

//    id("com.google.gms.google-services")
//    id("com.google.firebase.crashlytics")
   // id("dagger.hilt.android.plugin")
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
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    // implementation(project(mapOf("path" to ":app")))
   // implementation(project(mapOf("path" to ":app")))
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-dash:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation("androidx.leanback:leanback:1.0.0")




    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
//    implementation("com.google.dagger:hilt-android:2.42")
//    kapt("com.google.dagger:hilt-android-compiler:2.42")
//    implementation ("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
//   // kapt ("androidx.hilt:hilt-compiler:1.0.0")
    implementation ("com.airbnb.android:lottie:3.4.0")
    implementation( "com.google.dagger:hilt-android:2.47")
    kapt ("com.google.dagger:hilt-android-compiler:2.47")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
// retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
   // implementation ("com.squareup.okhttp3:logging-interceptor:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    implementation ("androidx.work:work-runtime-ktx:2.7.1")

    //firebase
//    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-crashlytics")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    implementation ("io.socket:socket.io-client:2.0.0")

    implementation ("com.github.SMehranB:AnimatedTextView:1.1.0")

    implementation ("nl.dionsegijn:konfetti-xml:2.0.4")


    implementation ("androidx.room:room-runtime:2.4.0")
    kapt ("androidx.room:room-compiler:2.4.0")
    implementation ("androidx.room:room-ktx:2.4.0")

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