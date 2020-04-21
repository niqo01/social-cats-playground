import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("js")
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

        implementation(npm("firebase", "7.14.1"))
        implementation(npm("react-firebaseui", "4.0.0"))
        implementation(npm("react", "16.13.1"))
        implementation(npm("react-dom", "16.13.1"))
        implementation(npm("react-router-dom", "5.1.2"))

        // TODO Should be dev dependency
        implementation(npm("style-loader", "1.0.0"))
        implementation(npm("css-loader", "3.2.0"))
    }
}

tasks {

    val zip = register("zipDistribution", Zip::class) {
        from("$buildDir/distributions")
        include("**")
    }

    zip.dependsOn("browserProductionWebpack")

    artifacts {
        archives(zip)
    }
}
