package com.nicolasmilliard.socialcats.auth

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
    val newUserHandler: NewUserHandler,
    val moshi: Moshi,
    val appInitializer: Initializer
)

class AppComponent(
    val module: AppModule = AppModule()
) {
    fun build(): Graph {
        val firebaseOptions = module.provideFirebaseOptions()
        // Can't move the following into initialize otherwise getting firestore fails
        FirebaseApp.initializeApp(firebaseOptions)
        val appInitializer = AppInitializer()
        val firestore = module.provideFirestore()
        val store = module.provideSocialCatsFirestoreAdmin(firestore)
        val moshi = module.provideMoshi()
        val newUserUseCase = module.provideNewUserUseCase(store)
        return Graph(newUserUseCase, moshi, appInitializer)
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

    fun provideNewUserUseCase(store: UserStoreAdmin) = NewUserHandler(store)

    fun provideSocialCatsFirestoreAdmin(firestore: Firestore): UserStoreAdmin = RealUserStoreAdmin(firestore)

    fun provideMoshi() = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .build()
}

interface Initializer {
    fun initialize()
}

class AppInitializer : Initializer {
    override fun initialize() {
    }
}
