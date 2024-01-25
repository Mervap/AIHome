plugins {
  id("com.android.application") version "8.2.2" apply false
  id("com.android.library") version "8.2.2" apply false
  kotlin("android") version "1.9.21" apply false
  kotlin("plugin.serialization") version "1.9.21" apply false
}

tasks.register<Delete>("clean") {
  delete(rootProject.buildDir)
}