import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.time.Instant

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.gmazzo.buildconfig") version "4.1.2"
    id("gg.jte.gradle") version "2.4.1"
    application
}

dependencies {
    // Keep using the libs.* for dependencies if libs.versions.toml is still available
    implementation(libs.bundles.shared)
    testImplementation(libs.bundles.sharedTest)

    implementation(libs.bundles.okhttp)
    implementation(libs.okio)

    implementation(libs.bundles.javalin)
    implementation(libs.bundles.jackson)

    implementation(libs.graphql.kotlin.server)
    implementation(libs.graphql.kotlin.scheme)
    implementation(libs.graphql.java.scalars)

    implementation(libs.bundles.exposed)
    implementation(libs.h2)
    implementation(libs.exposed.migrations)

    implementation(libs.bundles.systemtray)

    implementation(libs.injekt)
    implementation(libs.okhttp.core)
    implementation(libs.rxjava)
    implementation(libs.jsoup)

    implementation(libs.serialization.xml.core)
    implementation(libs.serialization.xml)

    implementation(libs.sort)
    implementation(libs.asm)
    implementation(libs.cache4k)
    implementation(libs.zip4j)
    implementation(libs.commonscompress)
    implementation(libs.junrar)
    implementation(libs.bouncycastle)

    implementation(projects.androidCompat)
    implementation(projects.androidCompat.config)

    implementation(projects.server.i18n)

    implementation(kotlin("script-runtime"))
    testImplementation(libs.mockk)
    implementation(libs.cron4j)
    implementation(libs.cronUtils)
    compileOnly(libs.kte)
}

jte {
    generate()
}

application {
    applicationDefaultJvmArgs = listOf(
        "-Djunrar.extractor.thread-keep-alive-seconds=30"
    )
    mainClass.set(MainClass)
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

buildConfig {
    className("BuildConfig")
    packageName("suwayomi.tachidesk.server.generated")
    useKotlinOutput()

    fun quoteWrap(obj: Any): String = """"$obj""""

    buildConfigField("String", "NAME", quoteWrap(rootProject.name))
    buildConfigField("String", "VERSION", quoteWrap(getTachideskVersion()))
    buildConfigField("String", "REVISION", quoteWrap(getTachideskRevision()))
    buildConfigField("String", "BUILD_TYPE", quoteWrap(if (System.getenv("ProductBuildType") == "Stable") "Stable" else "Preview"))
    buildConfigField("long", "BUILD_TIME", Instant.now().epochSecond.toString())
    buildConfigField("String", "WEBUI_TAG", quoteWrap(webUIRevisionTag))
    buildConfigField("String", "GITHUB", quoteWrap("https://github.com/Suwayomi/Suwayomi-Server"))
    buildConfigField("String", "DISCORD", quoteWrap("https://discord.gg/DDZdqZWaHA"))
}

tasks {
    shadowJar {
        isZip64 = true
        manifest {
            attributes(
                "Main-Class" to MainClass,
                "Implementation-Title" to rootProject.name,
                "Implementation-Vendor" to "The Suwayomi Project",
                "Specification-Version" to getTachideskVersion(),
                "Implementation-Version" to getTachideskRevision(),
            )
        }
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(getTachideskVersion())
        archiveClassifier.set("")
        destinationDirectory.set(File("$rootDir/server/build"))
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinJvmCompile> {
        compilerOptions {
            freeCompilerArgs.add(
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }

    named<Copy>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mustRunAfter("downloadWebUI")
    }

    register<Download>("downloadWebUI") {
        src("https://github.com/Suwayomi/Suwayomi-WebUI-preview/releases/download/$webUIRevisionTag/Suwayomi-WebUI-$webUIRevisionTag.zip")
        dest("src/main/resources/WebUI.zip")

        fun shouldOverwrite(): Boolean {
            val zipPath = project.projectDir.absolutePath + "/src/main/resources/WebUI.zip"
            val zipFile = net.lingala.zip4j.ZipFile(zipPath)

            var shouldOverwrite = true
            if (zipFile.isValidZipFile) {
                val zipRevision = zipFile.getInputStream(zipFile.getFileHeader("revision")).bufferedReader().use {
                    it.readText().trim()
                }
                if (zipRevision == webUIRevisionTag) {
                    shouldOverwrite = false
                }
            }
            return shouldOverwrite
        }

        overwrite(shouldOverwrite())
    }

    register("runElectron") {
        group = "application"
        finalizedBy(run)
        doFirst {
            application.applicationDefaultJvmArgs = listOf(
                "-Dsuwayomi.tachidesk.config.server.webUIInterface=electron",
                "-Dsuwayomi.tachidesk.config.server.electronPath=/usr/bin/electron",
            )
        }
    }

    runKtlintCheckOverMainSourceSet {
        mustRunAfter(generateJte)
    }
}

