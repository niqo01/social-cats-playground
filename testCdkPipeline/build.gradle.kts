subprojects {
    repositories {
        mavenCentral()
    }
    repositories {
        maven(url = "https://s3-us-west-2.amazonaws.com/dynamodb-local/release")
    }
}


plugins {
    alias(libs.plugins.bencheck)
//    alias(libs.plugins.taskinfo)
    alias(libs.plugins.kotlinjvm) apply false
}

