# Test Locally

# Stage

./gradlew :backend:ktor-api:build :backend:ktor-api:appengineStage

# Deploy App

./gradlew :backend:ktor-api:build  :backend:ktor-api:appengineDeploy

# Functional test

./gradlew :backend:ktor-api:functionalTest

# Curl Test Deployed app

curl  https://searchapi-dot-sweat-monkey.appspot.com
