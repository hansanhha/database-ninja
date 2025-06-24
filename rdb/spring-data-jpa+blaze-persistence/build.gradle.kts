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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")


    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql:42.7.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}