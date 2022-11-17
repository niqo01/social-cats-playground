import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.model.internal.core.ModelNodes.withType

class CheckUpdatesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.github.ben-manes.versions")

            tasks.withType(DependencyUpdatesTask::class.java) {
                rejectVersionIf {
                    isNonStable(candidate.version)
                }
                checkForGradleUpdate = true
                outputFormatter = "json"
                outputDir = "build/reports"
                reportfileName = "dependencyUpdates"
            }
        }
    }

    fun isNonStable(version: String): Boolean {
        val unStableKeyword = listOf("-ALPHA", "-BETA", "-RC").any { version.toUpperCase().contains(it) }
        return unStableKeyword
    }
}