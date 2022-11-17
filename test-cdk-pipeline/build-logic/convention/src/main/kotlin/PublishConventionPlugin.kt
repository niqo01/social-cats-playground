
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class PublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            extensions.configure<PublishingExtension> {

                repositories {
                    maven {
                        url =
                            uri("https://test-domain-480917579245.d.codeartifact.us-east-1.amazonaws.com/maven/test-repository/")
                        credentials {
                            username = "aws"
                            password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
                        }
                    }
                }
            }
        }
    }

}