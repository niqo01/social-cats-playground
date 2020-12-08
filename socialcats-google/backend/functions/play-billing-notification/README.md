# Build

./gradlew :backend:functions:play-billing-notification:build

# Deploy function

gcloud beta functions deploy AuthUserCreated \
--runtime java11 \
--entry-point com.nicolasmilliard.socialcats.auth.AuthUserCreatedFunction \
--trigger-topic projects/sweat-monkey/topics/play-billing-notification \
--memory=512MB \
--source=backend/functions/play-billing-notification/build/libs

# Delete User done via firebase extension

Delete is done via https://firebase.google.com/products/extensions/delete-user-data/