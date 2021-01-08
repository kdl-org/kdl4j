plugins {
    java
    antlr
}

group = "dev.hbeck.kdl"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.generateGrammarSource {
    outputDirectory = file("src/main/java/dev/hbeck/kdl/antlr")
    arguments = arguments + listOf("-visitor", "-no-listener")
}

dependencies {
    antlr("org.antlr:antlr4:4.9")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.antlr:antlr4:4.9")
//    testCompile("junit", "junit", "4.12")
}
