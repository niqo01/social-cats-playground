import org.gradle.api.Project
import java.io.File

fun Project.gitSha(): String {
    val f = File(project.rootProject.buildDir, "commit-sha.txt")
    if (!f.exists()) {
        val p = Runtime.getRuntime().exec("git rev-parse HEAD", null, project.rootDir)
        if (p.waitFor() != 0) {
            throw RuntimeException(p.errorStream.bufferedReader().use { it.readText() })
        }
        val input = p.inputStream.bufferedReader().use { it.readText().trim() }
        f.parentFile.mkdirs()
        f.writeText(input)
    }
    return f.readText()
}

fun Project.gitTimestamp(): String {
    val f = File(project.rootProject.buildDir, "commit-timestamp.txt")
    if (!f.exists()) {
        val p = Runtime.getRuntime().exec("git log -n 1 --format=%at", null, project.rootDir)
        if (p.waitFor() != 0) {
            throw RuntimeException(p.errorStream.bufferedReader().use { it.readText() })
        }
        val input = p.inputStream.bufferedReader().use { it.readText().trim() }
        f.parentFile.mkdirs()
        f.writeText(input)
    }
    return f.readText()
}


fun Project.getGCloudKeyFilePath(key: String): String {
    val f = File(project.rootProject.buildDir, "gcloud.json")
    if (!f.exists()) {
        f.parentFile.mkdirs()
        f.writeText(key)
    }
    return f.name
}
