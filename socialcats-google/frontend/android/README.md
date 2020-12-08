## Release to Firebase App Distribution

./gradlew :frontend:android:assembleRelease appDistributionUploadRelease

## Publish to Github repository
./gradlew :frontend:android:publish -Pgpr.user=niqo01 -Pgpr.key={}