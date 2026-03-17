tasks.withType<Jar> {
  archiveFileName.set("rschem-common.jar")
}

dependencies {
  "implementation"(libs.guava)

  "compileOnly"(libs.purpur)
  "compileOnly"(libs.bundles.adventure)
}
