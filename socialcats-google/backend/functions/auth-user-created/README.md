# Build

./gradlew :backend:functions:auth-user-created:build

# Deploy function

gcloud beta functions deploy AuthUserCreated \
--runtime java11 \
--entry-point com.nicolasmilliard.socialcats.auth.AuthUserCreatedFunction \
--trigger-event providers/firebase.auth/eventTypes/user.create \
--trigger-resource "sweat-monkey" \
--memory=512MB \
--source=backend/functions/auth-user-created/build/libs

# Delete User done via firebase extension

Delete is done via https://firebase.google.com/products/extensions/delete-user-data/