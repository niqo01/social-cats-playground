import org.gradle.api.Project

fun Project.findStringProperty(key: String) = findProperty(key) as String
fun Project.findIntProperty(key: String) = findStringProperty(key).toInt()