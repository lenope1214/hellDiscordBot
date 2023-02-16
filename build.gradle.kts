import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
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
    api("net.dv8tion:JDA:5.0.0-beta.3")

    // JDA KTX https://github.com/MinnDevelopment/jda-ktx/tags 버전확인
    api("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
    api("ch.qos.logback:logback-classic:1.2.8")
    api("com.squareup.okhttp3:okhttp:4.9.3")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api(kotlin("stdlib-jdk8"))

    // .env 사용을 위해
    api("io.github.cdimascio:java-dotenv:5.2.2")

//    testImplementation(kotlin("test"))
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