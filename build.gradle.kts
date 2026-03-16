allprojects {
  group = "me.verion.rschem"
  version = "2.0-SNAPSHOT"

  repositories {
    mavenCentral()
    maven("https://jitpack.io")

    maven("https://repo.purpurmc.org/snapshots")
  }
}

subprojects {
  apply(plugin = "java-library")

  dependencies {
    // the 'rootProject.libs.' prefix is needed here - see https://github.com/gradle/gradle/issues/16634
    // lombok
    "compileOnly"(rootProject.libs.lombok)
    "annotationProcessor"(rootProject.libs.lombok)
    // general
    "compileOnly"(rootProject.libs.annotations)
    // testing
    "testImplementation"(rootProject.libs.bundles.junit)
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

  extensions.configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
  }
}
