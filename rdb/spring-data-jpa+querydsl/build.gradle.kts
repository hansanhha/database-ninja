import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/plugins-release")
    }

    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:3.5.0")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    // 서브 프로젝트 공통 의존성
    dependencies {
        // 스프링 데이터 JPA 의존성
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "implementation"("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.1")

        // QueryDSL 의존성 (java 17+ jakarta)
        "implementation"("com.querydsl:querydsl-jpa:5.1.0:jakarta")
        "annotationProcessor"("com.querydsl:querydsl-apt:5.1.0:jakarta")
        "annotationProcessor"("jakarta.annotation:jakarta.annotation-api")
        "annotationProcessor"("jakarta.persistence:jakarta.persistence-api")

        "annotationProcessor"("org.projectlombok:lombok")

        "runtimeOnly"("com.h2database:h2")
        "runtimeOnly"("org.postgresql:postgresql:42.7.6")
    }

    // APT 설정 (Lombok, QueryDSL 등)
    configurations.configureEach {
        if (name == "compileOnly") {
            extendsFrom(configurations.getByName("annotationProcessor"))
        }
    }

    plugins.withType<JavaPlugin> {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events("STARTED", "PASSED", "FAILED", "SKIPPED")
        }
    }
}