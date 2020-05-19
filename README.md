# Social Cats playground 

Dummy social network playground use to investigate:
- Google Cloud Functions for Java
- ElasticSearch 
- Google App Engine second generation for Java
- Firebase Auth
- Ktor
- Kotlin everywhere

# TODOs
- auto deploy ktor and function
- cleanup Cloud functions resources like ES REST client close()
- Use flow share operator when ready
- Run dependency graph script in CI
- Publish Android library and applications, waiting on bug
- Use Kermit
- Use SharedFlow when ready https://github.com/Kotlin/kotlinx.coroutines/issues/2034

# Architecture 
- Module vs library

## JS
- try deploying new js plugin

## Android
- Handle exception in general
- Handle network error
- Handle Backend API error
- Instant enabled app bundle
- dynamic feature module
- Preferences
- App ratings Looks like play store will launch an in app rating feature
- Data saver
- Cache user list results (Consider new Store version https://github.com/dropbox/Store)
- Shared element transition https://medium.com/redmadrobot-mobile/hidden-mistakes-with-shared-element-transitions-65d79831c63
- Debug Menu https://github.com/willowtreeapps/Hyperion-Android
- Jake wharton diffuse apk
- margin 600dp screen
- espresso test per module
- Cloud messaging, function to send a notification
- Android 28 notification style message with person object
- Test UiBinding class
- Feature advance remote config usage
- Kaespresso https://github.com/KasperskyLab/Kaspresso
- try https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
- try https://github.com/maskarade/gradle-android-ribbonizer-plugin 
- try Dexter lib for handling permission  https://github.com/Karumi/Dexter-
- try Retrofit call adapter https://proandroiddev.com/create-retrofit-calladapter-for-coroutines-to-handle-response-as-states-c102440de37a

Extras:
- Jetpack compose test?

Testing:
- End to end test Android, ES
- Firebase Emulator CircleCi firebase test lab


# Issues
- Failed to Apply plugin in current context https://youtrack.jetbrains.com/issue/KT-33569
- Firebase Auth anonymous to existing user https://github.com/firebase/FirebaseUI-Android/issues/1702
- No Firebase Auth update triggers
- Read gradle.properties from buildSrc
- Cloud App engine debug instance
- Firebase inappmessage display depends on Picasso https://github.com/firebase/firebase-android-sdk/issues/1025
- https://youtrack.jetbrains.com/issue/KT-35855, https://youtrack.jetbrains.com/issue/KT-32209 org.jetbrains.kotlin.js does not respect Gradle's archivesBaseName