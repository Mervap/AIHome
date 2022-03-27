import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.serialization")
}

android {
  compileSdk = 32

  defaultConfig {
    applicationId = "me.mervap.ai.home"
    minSdk = 26
    targetSdk = 32
    versionCode = 5
    versionName = "2.1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    resourceConfigurations.clear()
    resourceConfigurations += arrayOf("us")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
      proguardFile("proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  packagingOptions {
    resources.pickFirsts.add("META-INF/*")
  }
}

dependencies {

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
  implementation("com.jjoe64:graphview:4.2.1")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

  implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
  implementation("com.google.android.material:material:1.5.0")

  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
  implementation("com.squareup.retrofit2:retrofit:2.9.0")

  implementation("androidx.core:core-ktx:1.7.0")
  implementation("androidx.fragment:fragment:1.4.1")
  implementation("androidx.appcompat:appcompat:1.4.1")
  implementation("androidx.preference:preference:1.2.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.3")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}