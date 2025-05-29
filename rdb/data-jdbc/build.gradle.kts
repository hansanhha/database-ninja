plugins {
    java
    id("org.springframework.boot").version("3.5.0")
    id("io.spring.dependency-management").version("1.1.7")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}