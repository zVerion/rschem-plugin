plugins {
  id("java")
  alias(libs.plugins.shadow)
}

group = "me.verion.rschem"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
  // lombok
  "compileOnly"(libs.lombok)
  "annotationProcessor"(libs.lombok)
  // general
  "compileOnly"(libs.annotations)
  "compileOnly"(libs.purpur)
  "implementation"(libs.rschem)
  // testing
  "testImplementation"(libs.bundles.junit)
}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_21.toString()
  targetCompatibility = JavaVersion.VERSION_21.toString()
  // options
  options.encoding = "UTF-8"
  options.isIncremental = true
}

tasks.getByName<Test>("test") {
  useJUnitPlatform()
}
