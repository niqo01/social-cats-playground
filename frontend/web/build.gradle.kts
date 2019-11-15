import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("js")
    id("distribution")
}

group = "com.nicolasmilliard.testjs"
version = "1.0-SNAPSHOT"

kotlin {
    target {
        useCommonJs()
        browser()
    }

    sourceSets["main"].dependencies {
        implementation(kotlin("stdlib-js"))

        implementation(Config.Libs.Kotlin.Coroutine.js)
        implementation(Config.Libs.WebFrontend.htmlJs)
        implementation(Config.Libs.WebFrontend.kotlinReact)
        implementation(Config.Libs.WebFrontend.kotlinReactDom)
        implementation(Config.Libs.WebFrontend.kotlinReactRouterDom)
        implementation(Config.Libs.KotlinLogging.js)

        implementation(npm("firebase", "7.3.0"))
        implementation(npm("react-firebaseui", "4.0.0"))
        implementation(npm("react", "16.11.0"))
        implementation(npm("react-dom", "16.11.0"))
        implementation(npm("react-router-dom", "5.1.2"))

        // TODO Should be dev dependency
        implementation(npm("style-loader", "1.0.0"))
        implementation(npm("css-loader", "3.2.0"))
    }
}

tasks {

    distributions {
        main {
            baseName = "main"

            contents {
                from("src/main/resources")
                from("$buildDir/distributions/web.js")
            }
        }
    }
    distTar {
        enabled = false
    }

    val unzip = register("unzipDist", Copy::class) {
        from(zipTree("$buildDir/distributions/main-1.0-SNAPSHOT.zip"))
        into("$buildDir/distributions")
    }

    distZip {
        finalizedBy(unzip)
        dependsOn("browserWebpack")
    }

    installDist.dependsOn("browserWebpack")
}
