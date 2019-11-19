object Config {
    const val kotlinVersion = "1.3.60"
    const val navigationVersion = "2.1.0"

    object GoogleCloud {
        const val projectId = "sweat-monkey"
    }

    object Versions {
        const val name = "0.1.0"

        object AndroidApp {
            const val major = 0
            const val minor = 0
            const val patch = 1
            const val build = 0

            const val name = "$major.$minor.$patch"
            const val fullName = "$name.$build"
            const val code = major * 1000000 + minor * 10000 + patch * 100 + build
        }

        object SearchApi {
            const val version = "20190628t1837"
        }
    }

    object Plugins {
        const val android = "com.android.tools.build:gradle:4.0.0-alpha03"
        const val appDistribution = "com.google.firebase:firebase-appdistribution-gradle:1.2.0"
        const val googleServices = "com.google.gms:google-services:4.3.3"
        const val fabric =  "io.fabric.tools:gradle:1.31.2"
        const val firebasePerformance = "com.google.firebase:perf-plugin:1.3.1"
        const val navigation =
            "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
        const val ktlintVersion = "0.35.0"
    }

    object Android {
        object SdkVersions {
            const val compile = 29
            const val target = 29
            const val min = 21
        }

        const val buildToolsVersion = "29.0.2"
    }

    object Libs {

        object WebFrontend {
            const val htmlJs = "org.jetbrains.kotlinx:kotlinx-html-js:0.6.12"
            const val kotlinReact = "org.jetbrains:kotlin-react:16.9.0-pre.87-kotlin-1.3.50"
            const val kotlinReactDom = "org.jetbrains:kotlin-react-dom:16.9.0-pre.87-kotlin-1.3.50"
            const val kotlinReactRouterDom =
                "org.jetbrains:kotlin-react-router-dom:4.3.1-pre.87-kotlin-1.3.50"
        }

        object GoogleFunction {
            private const val servletApi = "4.0.1"

            const val gson = "com.google.code.gson:gson:2.8.6"
            const val javaServletApi = "javax.servlet:javax.servlet-api:$servletApi"
            const val functionFrameworkApi =
                "com.google.cloud.functions:functions-framework-api:1.0.0-alpha-1"
        }

        object Ktor {
            private const val ktorVersion = "1.2.5"

            const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktorVersion"
            const val ktorServerTest = "io.ktor:ktor-server-tests:$ktorVersion"
            const val ktorAuth = "io.ktor:ktor-auth:$ktorVersion"
            const val ktorSerialization = "io.ktor:ktor-serialization:$ktorVersion"
            const val ktorLocation = "io.ktor:ktor-locations:$ktorVersion"
            const val ktorUtils = "io.ktor:ktor-utils:$ktorVersion"
        }

        object Kotlin {

            const val common = "org.jetbrains.kotlin:kotlin-stdlib-common:${kotlinVersion}"
            const val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}"
            const val js = "org.jetbrains.kotlin:kotlin-stdlib-js:${kotlinVersion}"

            object Test {
                const val common = "org.jetbrains.kotlin:kotlin-test-common:${kotlinVersion}"
                const val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common:${kotlinVersion}"
                const val jdk = "org.jetbrains.kotlin:kotlin-test-junit:${kotlinVersion}"
                const val js = "org.jetbrains.kotlin:kotlin-test-js:${kotlinVersion}"
            }

            object Coroutine {
                private const val version = "1.3.2-1.3.60-eap-76"

                const val common = "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$version"
                const val jdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$version"
                const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
                const val js = "org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$version"
                const val playServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$version"
                const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
            }

            object Serialization {
                private const val version = "0.13.0"

                const val common =
                    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${version}"
                const val jdk = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${version}"
                const val js = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${version}"

            }
        }

        object AndroidX {
            private const val pagingVersion = "2.1.0"
            private const val fragmentVersion = "1.1.0"
            private const val workVersion = "2.2.0"
            private const val lifecycle = "2.2.0-rc02"

            const val appCompat = "androidx.appcompat:appcompat:1.1.0"
            const val activityKtx = "androidx.activity:activity-ktx:1.0.0"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentVersion"
            const val fragmentTesting = "androidx.fragment:fragment-testing:$fragmentVersion"
            const val preferenceKtx = "androidx.preference:preference-ktx:1.1.0"
            const val vectorCompat = "androidx.vectordrawable:vectordrawable-animated:1.1.0"
            const val coreKtx = "androidx.core:core-ktx:1.1.0"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
            const val recyclerView = "androidx.recyclerview:recyclerview:1.1.0-rc01"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle"
            const val lifecycleKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle"
            const val lifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:$lifecycle"
            const val pagingRuntimeKtx = "androidx.paging:paging-runtime-ktx:$pagingVersion"
            const val pagingCommon = "androidx.paging:paging-common:$pagingVersion"
            const val dynamicAnimation =
                "androidx.dynamicanimation:dynamicanimation-ktx:1.0.0-alpha02"
            const val navigationFragmentKtx =
                "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
            const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
            const val workRuntimeKtx = "androidx.work:work-runtime-ktx:$workVersion"
            const val workTesting = "androidx.work:work-testing:$workVersion"
        }

        object PlayServices {
            const val ossLicenses = "com.google.android.gms:play-services-oss-licenses:17.0.0"
        }

        object Firebase {

            const val admin = "com.google.firebase:firebase-admin:6.10.0"

            const val common = "com.google.firebase:firebase-common-ktx:19.3.0"
            const val auth = "com.google.firebase:firebase-auth:19.1.0"
            const val firestore = "com.google.firebase:firebase-firestore-ktx:21.3.0"
            const val analytics = "com.google.firebase:firebase-analytics:17.2.1"
            const val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"
            const val performance = "com.google.firebase:firebase-perf:19.0.2"
            const val remoteConfig = "com.google.firebase:firebase-config-ktx:19.0.3"
            const val messaging = "com.google.firebase:firebase-messaging:20.0.1"
            const val inAppMessaging = "com.google.firebase:firebase-inappmessaging-display-ktx:19.0.2"

            const val uiAuth = "com.firebaseui:firebase-ui-auth:6.1.0"
            const val uiFirestore = "com.firebaseui:firebase-ui-firestore:6.1.0"
        }

        object Dagger {
            private const val version = "2.24"
            private const val assistedInjectVersion = "0.5.0"

            const val core = "com.google.dagger:dagger:$version"
            const val compiler = "com.google.dagger:dagger-compiler:$version"
            const val android = "com.google.dagger:dagger-android-support:$version"
            const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"

            const val assistedInject =
                "com.squareup.inject:assisted-inject-annotations-dagger2:$assistedInjectVersion"
            const val assistedInjectProcessor =
                "com.squareup.inject:assisted-inject-processor-dagger2:$assistedInjectVersion"
        }

        object LeakCanary {
            private const val version = "1.6.3"

            const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
            const val leakCanaryFragments =
                "com.squareup.leakcanary:leakcanary-support-fragment:$version"
            const val leakCanaryNoop =
                "com.squareup.leakcanary:leakcanary-android-no-op:$version"
        }

        object Test {
            const val truth = "com.google.truth:truth:1.0"
            const val junit = "junit:junit:4.12"
            const val androidxJunit = "androidx.test.ext:junit:1.1.1"
            const val androidxtruth = "androidx.test.ext:truth:1.2.0"
            const val robolectric = "org.robolectric:robolectric:4.3.1"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }

        object Aws {
            const val sdkBom = "software.amazon.awssdk:bom:2.10.19"
            const val apacheClient = "software.amazon.awssdk:apache-client"
            const val sdkAuth = "software.amazon.awssdk:auth"
        }

        object OkHttp {
            private const val version = "4.2.2"

            const val client = "com.squareup.okhttp3:okhttp:$version"
            const val logging = "com.squareup.okhttp3:logging-interceptor:$version"
        }

        object Retrofit {
            private const val version = "2.6.2"

            const val client = "com.squareup.retrofit2:retrofit:$version"
            const val converterKotlinxSerialization = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.4.0"
        }

        object KotlinLogging {
            private const val kotlinLoggingVersion = "1.7.8"
            const val common = "io.github.microutils:kotlin-logging-common:$kotlinLoggingVersion"
            const val jdk = "io.github.microutils:kotlin-logging:$kotlinLoggingVersion"
            const val js = "io.github.microutils:kotlin-logging-js:$kotlinLoggingVersion"
        }


        // Misc
        const val material = "com.google.android.material:material:1.1.0-beta02"
        const val timber = "com.jakewharton.timber:timber:4.7.1"
        const val picasso = "com.squareup.picasso:picasso:2.71828"

        const val okIo = "com.squareup.okio:okio:2.4.1"
        const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"


        const val logBackClassic = "ch.qos.logback:logback-classic:1.2.3"
        const val log4jToSlf4j = "org.apache.logging.log4j:log4j-to-slf4j:2.12.1"
        const val slf4jSimple= "org.slf4j:slf4j-simple:1.7.25"
        const val slf4jTimber = "com.arcao:slf4j-timber:3.1@aar"
        const val elasticSearchHighLevelClient =
            "org.elasticsearch.client:elasticsearch-rest-high-level-client:7.4.2"

        const val byteUnits = "com.jakewharton.byteunits:byteunits:0.9.1"

        const val playCore = "com.google.android.play:core-ktx:1.6.4"
    }
}
