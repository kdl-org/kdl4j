plugins {
    java
}

group = "dev.hbeck.kdl"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.antlr:antlr4:4.9")
    testImplementation("junit", "junit", "4.12")
}
