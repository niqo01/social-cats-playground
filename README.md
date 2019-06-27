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
- Replace Broadcast channels with DataFlow when ready
- Run dependency graph script in CI
- Publish Android library and applications, waiting on bug
- Figure out multi platform exceptions
- Module vs library

## JS
- try deploying new js plugin

## Android
- Handle FirebaseAuthRecentLoginRequiredException and exception in general
- Handle network error
- Handle Backend API error
- Instant enabled app bundle
- dynamic feature module
- Preferences
- App ratings Looks like play store will launch an in app rating feature
- Data saver
- Cache user list results
- Shared element transition https://medium.com/redmadrobot-mobile/hidden-mistakes-with-shared-element-transitions-65d79831c63
- Debug Menu https://github.com/willowtreeapps/Hyperion-Android
- Jake wharton diffuse apk
- margin 600dp screen
- espresso test per module
- Cloud messaging, function to send a notification
- Android 28 notification style message with person object
- Test UiBinding class
- Firebase analytics & analytics module
- Feature advance remote config usage

Extras:
- Jetpack compose test?

Testing:
- End to end test Android, ES
- Firebase Emulator CircleCi firebase test lab


# Issues
- Failed to Apply plugin in current context https://youtrack.jetbrains.com/issue/KT-33569
- Firebase Auth anonymous to existing user https://github.com/firebase/FirebaseUI-Android/issues/1702
- SessionManager token test flaky, adding a log fix the issue ...
- No Firebase Auth update triggers
- Read gradle.properties from buildSrc
- Android gradle publish issue: https://issuetracker.google.com/issues/144790367
- Cloud App engine debug instance
- Firebase inappmessage display depends on Picasso https://github.com/firebase/firebase-android-sdk/issues/1025
- Handle delete account sign in required