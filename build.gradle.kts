import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
}

application {
    mainClass.set("kr.weareboard.hellbot.Main")
}

group = "kr.weareboard"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal() // caching optimization
    mavenCentral() // everything else
    maven("https://m2.dv8tion.net/releases") // jda
    maven("https://jitpack.io") // jda-reactor and slash commands
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.3"){
        exclude(module="opus-java")
    }

    // JDA KTX https://github.com/MinnDevelopment/jda-ktx/tags 버전확인
    implementation("com.github.minndevelopment:jda-ktx:0.10.0-beta.1")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}