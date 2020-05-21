# Build

./gradlew :backend:functions:firestore-user-changed:build

# Deploy function

gcloud alpha functions deploy FirestoreUserChanged \
--runtime java11 \
--entry-point com.nicolasmilliard.socialcats.FirestoreUserChangedFunction \
--trigger-event providers/cloud.firestore/eventTypes/document.write \
--trigger-resource "projects/sweat-monkey/databases/(default)/documents/users/{userId}" \
--source=backend/functions/firestore-user-changed/build/libs \
--memory=512MB \
--set-env-vars "ES_ENDPOINT=...,ES_API_KEY_ID=...,ES_API_KEY=...,STRIPE_PUBLIC_KEY=...,STRIPE_PRIVATE_KEY=..."