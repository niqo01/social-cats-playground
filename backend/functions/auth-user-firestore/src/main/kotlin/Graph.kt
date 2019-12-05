package com.nicolasmilliard.socialcats

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.nicolasmilliard.socialcats.store.RealUserStoreAdmin
import com.nicolasmilliard.socialcats.store.UserStoreAdmin

data class Graph(
    val store: UserStoreAdmin
)

class AppComponent(
    val module: AppModule = AppModule()
) {
    fun build(): Graph {
        val firebaseOptions = module.provideFirebaseOptions()
        FirebaseApp.initializeApp(firebaseOptions)
        val firestore = module.provideFirestore()
        val socialCatsFirestoreAdmin = module.provideSocialCatsFirestoreAdmin(firestore)
        return Graph(socialCatsFirestoreAdmin)
    }
}

class AppModule {

    fun provideFirebaseOptions(): FirebaseOptions {
        val credentials = GoogleCredentials.getApplicationDefault()
        return FirebaseOptions.Builder()
            .setCredentials(credentials)
            .setProjectId("sweat-monkey")
            .build()
    }

    fun provideFirestore(): Firestore = FirestoreClient.getFirestore()

    fun provideSocialCatsFirestoreAdmin(firestore: Firestore): UserStoreAdmin = RealUserStoreAdmin(firestore)
}
