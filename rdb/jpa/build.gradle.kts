plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    runtimeOnly("org.junit.platform:junit-platform-launcher:")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}