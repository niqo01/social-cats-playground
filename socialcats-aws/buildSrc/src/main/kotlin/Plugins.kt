object Plugins {
    object Ids {
        const val ANDROID_APPLICATION = "com.android.application"
        const val ANDROID_DYNAMIC_FEATURE = "com.android.dynamic-feature"
        const val ANDROID_LIBRARY = "com.android.library"
        const val KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
        const val KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        const val KOTLIN_MULTIPLATFORM = "org.jetbrains.kotlin.multiplatform"
    }
}
val ANDROID_PLUGIN_IDS = setOf(
    Plugins.Ids.ANDROID_APPLICATION,
    Plugins.Ids.ANDROID_LIBRARY,
    Plugins.Ids.ANDROID_DYNAMIC_FEATURE
)