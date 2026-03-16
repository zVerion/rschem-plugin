tasks.withType<Jar> {
  archiveFileName.set("rschem-command.jar")
}

dependencies {
  "api"(projects.api)

  "compileOnly"(libs.purpur)
}
