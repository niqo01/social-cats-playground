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
- Handle network error
- Handle Backend API error
- Instant enabled app bundle
- dynamic feature module
- Preferences
- oss licence
- App ratings Looks like play store will launch an in app rating feature
- Image management Coil, picasso?
- Data saver
- Move Loading fragment sign in code into MainActivity
- Cache user list results
- Cloud messaging
- analytics module
- Shared element transition https://medium.com/redmadrobot-mobile/hidden-mistakes-with-shared-element-transitions-65d79831c63
- Feature flags https://jeroenmols.com/blog/2019/09/12/featureflagsarchitecture/ or wait for firebase
- Firebase app distribution
- Debug Menu https://github.com/willowtreeapps/Hyperion-Android
- Jake wharton diffuse apk
- margin 600dp screen
- Jetpack compose test?
- Replace toModel to constructor
- Firebase auth anonymous
- Firebase Remote config 
- Firebase analytics
- Firebase Inapp message
- Handle FirebaseAuthRecentLoginRequiredException
- Google play internal deploy
- Make view dumber and model reflect view better (Possibly using several model, one for presenter, one for view)

Testing:
- End to end test Android, ES
- Firebase Emulator CircleCi firebase test lab
- test firestore rules

# Issues
- Failed to Apply plugin in current context https://youtrack.jetbrains.com/issue/KT-33569
- aws gradle build issue https://github.com/gradle/gradle/issues/10951
- lint 
- firebase app distribution