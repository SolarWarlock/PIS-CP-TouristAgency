import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// В проекте kotlin("jvm") зависимости пишутся в верхнеуровневом блоке dependencies
dependencies {
    // Compose (определяет ОС автоматически)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // === 1. БАЗА ДАННЫХ (PostgreSQL + Exposed) ===
    implementation("org.postgresql:postgresql:42.7.2") // Драйвер
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1") // Даты

    // === 2. НАВИГАЦИЯ (Voyager) ===
    val voyagerVersion = "1.0.0"
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion") // MVVM
    implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion") // Анимации
    implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion") // Связь с DI

    // === 3. ВНЕДРЕНИЕ ЗАВИСИМОСТЕЙ (Koin) ===
    implementation("io.insert-koin:koin-core:3.5.3")

    // === 4. АСИНХРОННОСТЬ (Coroutines) ===
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // === 5. ОТЧЕТЫ (Excel / Apache POI) ===
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // === 6. ЛОГИРОВАНИЕ (Чтобы видеть SQL запросы) ===
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.slf4j:slf4j-api:2.0.12")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TravelAgency"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("C:/Users/54321/Desktop/Univer/5_semester/PIS/CP/TravelAgency/build/resources/main/logo.ico"))
            }
        }
    }
}