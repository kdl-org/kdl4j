plugins {
    java
    jacoco
}

group = "dev.hbeck.kdl"
version = "0.1.0"

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
    testImplementation("junit", "junit", "4.12")
}
