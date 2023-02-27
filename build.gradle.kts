import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion: String by System.getProperties() // 1.8.10

    kotlin("jvm") version kotlinVersion
    id("java")
//    application
}

//application {
//    mainClass.set("kr.wearebaord.hellbot.Botkt")
//}

group = "kr.weareboard"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal() // caching optimization
    mavenCentral() // everything else
    maven("https://m2.dv8tion.net/releases") // jda
    maven("https://jitpack.io") // jda-reactor and slash commands
}

dependencies {
    val kotlinVersion = System.getProperties() // 1.8.10
    val junitVersion = System.getProperties() // 4.13.2
    val jdaVersion = System.getProperties() // 5.0.0-beta.4
    val dotenvVersion = System.getProperties() // 6.4.1

    api("net.dv8tion:JDA:${jdaVersion}")

    // JDA KTX https://github.com/MinnDevelopment/jda-ktx/tags 버전확인
    api("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
    api("ch.qos.logback:logback-classic:1.2.8")
    api("com.squareup.okhttp3:okhttp:4.9.3")

    // YOUTUBE MUSIC SUPPORT https://github.com/sedmelluq/lavaplayer

    api("com.sedmelluq:lavaplayer:1.3.77")

    // .env 사용을 위해
    implementation("io.github.cdimascio:dotenv-kotlin:${dotenvVersion}")

    // kotlin
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api(kotlin("stdlib-jdk8"))

    // test
    testApi("junit:junit:${junitVersion}")
    testApi("org.jetbrains.kotlin:kotlin-test-junit:${kotlinVersion}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

// 실행 가능하게 하도록 설정
tasks.jar {
    manifest {
        attributes["Main-Class"] = "kr.wearebaord.hellbot.HellBotKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveFileName.set("hellbot.jar")
}