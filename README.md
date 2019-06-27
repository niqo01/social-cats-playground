# Social Cats playground 

Dummy social network playground use to investigate:
- Google Cloud Functions for Java
- ElasticSearch / Redis
- Google App Engine second generation for Java
- Firebase Auth
- Ktor
- Kotlin everywhere

# TODOs
- Figure out better multi-platform logging system (Timber?)
- auto deploy ktor and function

## JS
- Migrate Js frontend to new plugin

## Android
- Handle FirebaseAuthRecentLoginRequiredException and exception in general
- Handle network error
- Handle Backend API error
- Instant enabled app bundle
- dynamic feature module
- Preferences
- App ratings Looks like play store will launch an in app rating feature
- Image management Coil, picasso?
- Data saver
- Move Loading fragment sign in code into MainActivity
- Cache user list results
- Shared element transition https://medium.com/redmadrobot-mobile/hidden-mistakes-with-shared-element-transitions-65d79831c63
- Feature flags https://jeroenmols.com/blog/2019/09/12/featureflagsarchitecture/ or wait for firebase
- Debug Menu https://github.com/willowtreeapps/Hyperion-Android
- Jake wharton diffuse apk
- margin 600dp screen
- Jetpack compose test?
- Replace toModel to constructor
- Firebase Remote config 
- Firebase analytics & analytics module
- Google play internal deploy Triple T play release
- Make view dumber and model reflect view better (Possibly using several model, one for presenter, one for view)
- Unit testing improvement
- espresso test per module
- Cloud messaging, function to send a notification
- Enforce Auth token on api call

Testing:
- End to end test Android, ES
- Firebase Emulator CircleCi firebase test lab
- test firestore rules

# Issues
- Failed to Apply plugin in current context https://youtrack.jetbrains.com/issue/KT-33569
- aws gradle build issue https://github.com/gradle/gradle/issues/10951
- firebase app distribution
- Firebase Auth anonymous to existing user https://github.com/firebase/FirebaseUI-Android/issues/1702