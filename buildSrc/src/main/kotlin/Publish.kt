import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get

fun Project.configurePublish() {
    afterEvaluate {
        configure<PublishingExtension> {
            repositories {
                s3(this@configurePublish)
            }

            publications {
                when {
                    plugins.hasPlugin(Plugins.Ids.KOTLIN_MULTIPLATFORM) -> {
                        // Kotlin multiplatform automatically creates configurations
                    }
                    plugins.hasPlugin(Plugins.Ids.ANDROID_LIBRARY) -> {
                        // TODO Issue https://issuetracker.google.com/issues/144790367
//                        logger.error("Create MavenPublication for Android library $name : Components size: ${components.size}")
//                        create<MavenPublication>("release") {
//                            groupId = Config.group
//                            artifactId = "${rootProject.name}-${project.name}"
//                            version = project.version.toString()
//
//                            from(components["release"])
//                        }
//
//                        create<MavenPublication>("debug") {
//                            groupId = Config.group
//                            artifactId = "${rootProject.name}-${project.name}"
//                            version = project.version.toString()
//
//                            from(components["debug"])
//                        }
                    }
                    plugins.hasPlugin("java") -> {
                        create<MavenPublication>("maven") {
                            from(components["java"])
                        }
                    }
                }
            }

        }
    }
}

private fun RepositoryHandler.s3(project: Project) {
    maven {
        val isReleaseBuild = !(project.version as String).endsWith("-SNAPSHOT")

        name = "S3Storage"
        url = project.uri(
            if (isReleaseBuild)
                "s3://repo-social-cats.s3.amazonaws.com/releases"
            else
                "s3://repo-social-cats.s3.amazonaws.com/snapshots"
        )
        credentials(AwsCredentials::class) {
            accessKey = project.findProperty("s3storage.access") as String? ?: System.getenv("S3_STORAGE_ACCESS")
            secretKey = project.findProperty("s3storage.secret") as String? ?: System.getenv("S3_STORAGE_SECRET")
        }
    }
}

private fun RepositoryHandler.github(project: Project) {
    maven {
        name = "GitHubPackages"
        url = project.uri("https://maven.pkg.github.com/niqo01/social-cats-playground")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
        }
    }
}
