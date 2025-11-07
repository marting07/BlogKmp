import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

val ktorVersion = "3.3.2"
val serializationJsonVersion = "1.7.3"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationJsonVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
            }
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-cio:${ktorVersion}")
            implementation("org.slf4j:slf4j-api:2.0.16")
            runtimeOnly("ch.qos.logback:logback-classic:1.5.12")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
            implementation("org.slf4j:slf4j-api:2.0.16")
            implementation("org.slf4j:slf4j-android:1.7.36")
        }
        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        iosSimulatorArm64Main {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        iosArm64Main {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        jsMain.dependencies {
            implementation("io.ktor:ktor-client-js:${ktorVersion}")
        }
        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:${ktorVersion}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.blogkmp.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
dependencies {
    implementation(libs.junit.junit)
}
