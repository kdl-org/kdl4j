plugins {
	`java-library`
	jacoco
	`maven-publish`
}

group = "dev.kdl"
version = "1.0.0-SNAPSHOT"

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
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.compileJava {
	options.javaModuleVersion = provider { version as String }
}

tasks.test {
	useJUnitPlatform()
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
	implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.assertj:assertj-core:3.25.3")
}
