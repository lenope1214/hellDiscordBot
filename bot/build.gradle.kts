plugins {
    val kotlinVersion: String by System.getProperties() // 1.8.10
    kotlin("jvm") version kotlinVersion
}
repositories {
    mavenCentral() // everything else
    maven("https://m2.dv8tion.net/releases") // jda
    maven("https://jitpack.io") // jda-reactor and slash commands
}

dependencies {
    val querydslVersion: String by System.getProperties() // 5.0.0
    val kotlinVersion: String by System.getProperties() // 1.8.10
    val junitVersion: String by System.getProperties() // "4.13.2"
    val jdaVersion: String by System.getProperties() // "5.0.0-beta.11"
    val dotenvVersion: String by System.getProperties() // "6.4.1"

    api(project(":domain"))

    // JDA : Java Discord API
    implementation("net.dv8tion:JDA:$jdaVersion")

    // JDA KTX https://github.com/MinnDevelopment/jda-ktx/tags 버전확인
    implementation("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // YOUTUBE MUSIC SUPPORT https://github.com/sedmelluq/lavaplayer

//    api("com.sedmelluq:lavaplayer:1.3.77")
    implementation("com.github.walkyst:lavaplayer-fork:1.4.0") // lavaplayer - fork project

    // .env 사용을 위해
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvVersion")

    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation(kotlin("stdlib-jdk8"))

    // jpa가 사용하는 javax가 jakarta로 변경됨에 따라 아래 어노테이션 프로세서를 추가해줘야 한다.
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")

    // test
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks.withType<Jar> {
    enabled = false
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
}
