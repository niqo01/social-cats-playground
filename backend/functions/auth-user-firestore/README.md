# Build

./gradlew :backend:functions:auth-user-firestore:shadowJar

# Deploy function

gcloud functions deploy AuthUserCreated \
--runtime java8 \
--entry-point com.nicolasmilliard.socialcats.AuthUserCreatedFunction.onUserCreated \
--trigger-event providers/firebase.auth/eventTypes/user.create \
--trigger-resource "sweat-monkey" \
--source=backend/functions/auth-user-firestore/build/libs

gcloud functions deploy AuthUserDeleted \
--runtime java8 \
--entry-point com.nicolasmilliard.socialcats.AuthUserDeletedFunction.onUserDeleted \
--trigger-event providers/firebase.auth/eventTypes/user.delete \
--trigger-resource "sweat-monkey" \
--source=backend/functions/auth-user-firestore/build/libs