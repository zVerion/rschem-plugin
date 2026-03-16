tasks.withType<Jar> {
  archiveFileName.set("rschem-api.jar")
}

dependencies {
  "api"(projects.common)

  "compileOnly"(libs.purpur)
  "compileOnly"(libs.bundles.adventure)
}
