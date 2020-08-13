# Installation
npm install webpack ts-loader --save-dev
npm install -g firebase-tools
firebase login

# Build
npx webpack

# Deploy
firebase deploy --only hosting:social-cats-payment

