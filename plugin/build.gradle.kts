import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  alias(libs.plugins.shadow)
}

tasks.withType<ShadowJar> {
  archiveFileName.set("rschem-${project.version}")
}

repositories {
  mavenCentral()
}

dependencies {
  "compileOnly"(libs.purpur)
}

tasks.test {
  useJUnitPlatform()
}
