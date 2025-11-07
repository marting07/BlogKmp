plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

val ktorVersion = "3.3.2"

group = "org.example.blogkmp"
version = "1.0.0"
application {
    mainClass.set("org.example.blogkmp.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation("io.ktor:ktor-server-cors-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${ktorVersion}")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}