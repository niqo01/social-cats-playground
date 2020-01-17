package com.nicolasmilliard.socialcats

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.nicolasmilliard.socialcats.store.RealUserStoreAdmin
import com.nicolasmilliard.socialcats.store.UserStoreAdmin
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.Date

data class Graph(
    val store: UserStoreAdmin,
    val moshi: Moshi
)

class AppComponent(
    val module: AppModule = AppModule()
) {
    fun build(): Graph {
        val firebaseOptions = module.provideFirebaseOptions()
        FirebaseApp.initializeApp(firebaseOptions)
        val firestore = module.provideFirestore()
        val socialCatsFirestoreAdmin = module.provideSocialCatsFirestoreAdmin(firestore)
        val moshi = module.provideMoshi()
        return Graph(socialCatsFirestoreAdmin, moshi)
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

    fun provideMoshi() = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .build()
}
