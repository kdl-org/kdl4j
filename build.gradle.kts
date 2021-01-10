plugins {
    java
    jacoco
}

group = "dev.hbeck.kdl"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacoco/coverage")
    }
}

dependencies {
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.antlr:antlr4:4.9")
    testImplementation("junit", "junit", "4.12")
}
