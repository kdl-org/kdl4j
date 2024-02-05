plugins {
    `java-library`
    jacoco
    `maven-publish`
}

group = "kdl"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kdl-org/kdl4j")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

java {
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacoco/coverage")
    }
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    testImplementation("org.mockito", "mockito-core", "3.7.7")
}
