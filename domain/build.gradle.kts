

val querydslVersion: String by System.getProperties()

plugins {
    val kotlinVersion: String by System.getProperties() // 1.8.10
    kotlin("kapt")
    kotlin("plugin.jpa")

    // intellij idea에서 사용할 수 있도록 추가
    idea
    kotlin("jvm") version kotlinVersion
}

// allopen setting 1
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity") // @Entity가 붙은 클래스에 한해서만 no arg 플러그인을 적용
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

dependencies {
    val kapt by configurations

    // spring-boot-starter
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jdbc")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api("org.springframework.security:spring-security-oauth2-client:6.0.1") // security.oauth2 사용을 위해 추가
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-validation") // 파라미터 값 확인(인증, Bean Validation)을 위해
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.1")

    // databases
    api("org.mariadb.jdbc:mariadb-java-client:3.1.0")

    // querydsl javax -> jakarta로 변경됨에 따라 :jakarta 추가
    api("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    api("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    api("com.querydsl:querydsl-kotlin-codegen:$querydslVersion") // kotlin code generation support
    // javax -> jakarta로 변경을 위해 :jpa -> :jakarta
    kapt("com.querydsl:querydsl-apt:$querydslVersion:jakarta") // 이게 없으면 build해도 Q class가 생성되지 않는다.

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation(kotlin("stdlib"))
}
repositories {
    mavenCentral()
}
