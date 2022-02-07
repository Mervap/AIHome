plugins {
  id("com.android.application") version "7.1.1" apply false
  id("com.android.library") version "7.1.1" apply false
  kotlin("android") version "1.6.10" apply false
  kotlin("plugin.serialization") version "1.6.10" apply false
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}