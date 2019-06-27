# Build

./gradlew :backend:functions:firestore-user-elasticsearch:shadowJar

# Deploy function

gcloud functions deploy FirestoreUserWritten \
--runtime java8 \
--entry-point com.nicolasmilliard.socialcats.FirestoreUserWrittenFunction.onUserWritten \
--trigger-event providers/cloud.firestore/eventTypes/document.write \
--trigger-resource "projects/sweat-monkey/databases/(default)/documents/users/{userId}" \
--source=backend/functions/firestore-user-elasticsearch/build/libs \
--set-env-vars "ES_ENDPOINT=...,ES_API_KEY_ID=...,ES_API_KEY=..."
OR
--set-env-vars AWS_ACCESS_KEY_ID=...,AWS_SECRET_ACCESS_KEY=...,AES_ENDPOINT=...,AES_REGION=...,AES_SERVICE_NAME=..."