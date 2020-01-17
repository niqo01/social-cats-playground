package com.nicolasmilliard.socialcats

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.nicolasmilliard.socialcats.data.AwsInterceptorModule
import com.nicolasmilliard.socialcats.data.ElasticServiceInterceptorModule
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.search.repository.ElasticSearchRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import java.util.Date
import org.apache.http.HttpHost
import org.apache.http.HttpRequestInterceptor
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

data class Graph(
    val searchUseCase: SearchUseCase,
    val esClient: RestHighLevelClient,
    val moshi: Moshi
)

class AppComponent(
    val module: AppModule = AppModule()
) {
    fun build(): Graph {
//        val firebaseOptions = module.provideFirebaseOptions()
//        Firebase.initializeApp(firebaseOptions)
//        val firestore = module.provideFirestore()
        val useAws = System.getenv("USE_AWS") == "true"
        val esClient = if (useAws) {
            val serviceName = System.getenv("AES_SERVICE_NAME")
            val region = System.getenv("AES_REGION")
            val interceptor = AwsInterceptorModule.provideAwsInterceptor(serviceName, region)
            module.provideEsClient(module.provideAwsEndpoint(), interceptor)
        } else {
            val keyId = System.getenv("ES_API_KEY_ID")
            val apiKey = System.getenv("ES_API_KEY")
            val interceptor = ElasticServiceInterceptorModule.provideElasticServiceInterceptor(keyId, apiKey)
            module.provideEsClient(module.provideEsEndpoint(), interceptor)
        }

        val searchRepository = module.provideSearchRepository(esClient)
        val searchUseCase = module.provideSearchUseCase(searchRepository)
        val moshi = module.provideMoshi()
        return Graph(searchUseCase, esClient, moshi)
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

    fun provideFirestore(): Firestore {
        return FirestoreClient.getFirestore()
    }

    fun provideAwsEndpoint() = System.getenv("AES_ENDPOINT")

    fun provideEsEndpoint() = System.getenv("ES_ENDPOINT")

    fun provideEsClient(endpoint: String, interceptor: HttpRequestInterceptor): RestHighLevelClient {
        return RestHighLevelClient(
            RestClient.builder(HttpHost.create(endpoint))
                .setHttpClientConfigCallback { hacb ->
                    hacb.addInterceptorLast(
                        interceptor
                    )
                })
    }

    fun provideSearchRepository(esClient: RestHighLevelClient) =
        ElasticSearchRepository(esClient)

    fun provideSearchUseCase(repository: ElasticSearchRepository) =
        SearchUseCase(repository)

    fun provideMoshi(): Moshi {
        val firestoreValueJsonAdapter = FirestoreValueJsonAdapter()
        return Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(FieldsAdapter())
            .add(firestoreValueJsonAdapter)
            .add(FirestoreEventJsonAdapter(firestoreValueJsonAdapter))
            .build()
    }
}
