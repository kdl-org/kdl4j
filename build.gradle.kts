plugins {
    java
    jacoco
    `maven-publish`
}

group = "dev.hbeck.kdl"
version = "0.2.0"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/hkolbeck/kdl4j")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
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
    testImplementation("org.mockito", "mockito-core", "3.7.7")
}
