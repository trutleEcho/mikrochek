import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                
                // SQLite
                implementation("org.xerial:sqlite-jdbc:3.44.1.0")
            }
        }
        
        commonMain {
            dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            /** Custom dependencies. */
            // MongoDB
            implementation("org.litote.kmongo:kmongo:4.11.0") // KMongo core
            implementation("org.litote.kmongo:kmongo-coroutine:4.11.0") // KMongo coroutine support

            // MongoDB BSON Codec for Kotlin
            implementation("org.mongodb:bson:4.5.0")

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0") // Use latest stable

            // (Optional) For Kotlin serialization support with KMongo
            implementation("org.litote.kmongo:kmongo-serialization:4.11.0")

            // (Optional) For better logging
            implementation("org.slf4j:slf4j-simple:2.0.12")

            // Koin for Multiplatform
            implementation("io.insert-koin:koin-core:3.2.0")
            implementation("org.jetbrains.compose.foundation:foundation:1.0.0") // Compose foundation for Desktop
            implementation("org.jetbrains.compose.material:material:1.0.0") // Compose material for Desktop
            implementation("io.insert-koin:koin-logger-slf4j:3.1.2") // For logging

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // Compatible with Kotlin 2.1.10

            // jBCrypt for password hashing
            implementation("org.mindrot:jbcrypt:0.4")
        }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.mikrochek.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "com.mikrochek"
            packageVersion = "1.1.1"
            jvmArgs += listOf("-Xmx512m")
            modules("java.sql") // <-- ensure java.sql module is included if using modular JVM
        }
    }
}
