package com.nicolasmilliard.publish

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets


fun Project.checkIfAlreadyPublished(artifactName: String): Boolean {
    val eout = ByteArrayOutputStream()
    val res = exec {
        commandLine(
            "aws",
            "codeartifact",
            "describe-package-version",
            "--domain", "test-domain",
            "--repository", "test-repository",
            "--format", "maven",
            "--namespace", "${project.group}",
            "--package", "$artifactName",
            "--package-version", "${project.version}"
        )
        isIgnoreExitValue = true
        standardOutput = ByteArrayOutputStream()
        errorOutput = eout
    }
    val errorOutput = eout.toString(StandardCharsets.UTF_8)
    return when {
        res.exitValue == 254 && errorOutput.contains("ResourceNotFoundException") -> true
        res.exitValue == 0 -> false
        else -> {
            res.rethrowFailure()
            false
        }
    }
}
