import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties() // 1.8.10
    val springBootVersion: String by System.getProperties() // 3.0.4

    id("java")
    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1" // kotlin lint

    kotlin("jvm") version kotlinVersion apply false                   // 1.8.10
    kotlin("kapt") version kotlinVersion apply false                  // 1.8.10
    kotlin("plugin.spring") version kotlinVersion apply false         // 1.8.10
    kotlin("plugin.jpa") version kotlinVersion apply false            // 1.8.10
}

group = "kr.weareboard"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {

    apply {
        plugin("io.spring.dependency-management")
        plugin("org.springframework.boot")

        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        verbose.set(true)
        disabledRules.set(
            setOf(
                "import-ordering",
                "no-wildcard-imports",
                "final-newline",
                "insert_final_newline",
                "max_line_length"
            )
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // plain jar 기본 true
    tasks.withType<Jar> {
        enabled = true
        // build 중에 중복되는 파일이 생성될경우 에러가 발생한다. 그것을 방지하기 위한 설정이다.
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // BootJar 기본 false, 프로젝트 빌드 후 실행해야 하는 모듈이면 BootJar true 해줘야 함.
    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        enabled = false
    }
}
