# Build

./gradlew :backend:functions:auth-user-firestore:shadowJar

# Deploy function

gcloud functions deploy AuthUserCreated \
--runtime java8 \
--entry-point com.nicolasmilliard.socialcats.AuthUserCreatedFunction.onUserCreated \
--trigger-event providers/firebase.auth/eventTypes/user.create \
--trigger-resource "sweat-monkey" \
--source=backend/functions/auth-user-firestore/build/libs


# Delete 

Delete is done via https://firebase.google.com/products/extensions/delete-user-data/