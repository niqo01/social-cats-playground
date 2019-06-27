package com.nicolasmilliard.socialcats.searchapi

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.nicolasmilliard.socialcats.data.AwsInterceptorModule
import com.nicolasmilliard.socialcats.data.ElasticServiceInterceptorModule
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.search.repository.ElasticSearchRepository
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import io.ktor.application.Application
import io.ktor.application.ApplicationStopped
import mu.KotlinLogging
import org.apache.http.HttpHost
import org.apache.http.HttpRequestInterceptor
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

data class Graph(
    val searchUseCase: SearchUseCase,
    val esClient: RestHighLevelClient,
    val firebaseTokenVerifier: FirebaseTokenVerifier
)

private val logger = KotlinLogging.logger {}

class AppComponent(
    private val application: Application,
    private val appModule: AppModule = AppModule()
) {
    fun build(): Graph {
        val isProduction = application.environment.config
            .property("env.isProduction").getString() == "true"
        val useAwsEs = application.environment.config
            .property("elasticSearch.useAws").getString() == "true"
        val endpoint = application.environment.config.property("elasticSearch.endpoint").getString()
        logger.info { "Endpoint: $endpoint" }
        val esClient = if (useAwsEs) {
            logger.info { "Using Aws Elastic Search" }
            val serviceName = application.environment.config.property("elasticSearch.serviceName").getString()
            val region = application.environment.config.property("elasticSearch.region").getString()

            appModule.provideEsClient(endpoint, AwsInterceptorModule.provideAwsInterceptor(serviceName, region))
        } else {
            logger.info { "Using ElasticService" }
            val id = application.environment.config.property("elasticSearch.apiKeyId").getString()
            val apiKey = application.environment.config.property("elasticSearch.apiKey").getString()
            appModule.provideEsClient(
                endpoint,
                ElasticServiceInterceptorModule.provideElasticServiceInterceptor(id, apiKey)
            )
        }

        val searchRepository = appModule.provideSearchRepository(
            esClient
        )
        val searchUseCase = appModule.provideSearchUseCase(searchRepository)
        val tokenVerifier = appModule.provideFirebaseTokenVerifier()

        application.environment.monitor.subscribe(ApplicationStopped) {
            println("Time to clean up")
            esClient.close()
        }

        return Graph(searchUseCase, esClient, if (isProduction) tokenVerifier else AllowByPassTokenAuth(tokenVerifier))
    }
}

open class AppModule {

    open fun provideEsClient(endpoint: String, interceptor: HttpRequestInterceptor): RestHighLevelClient {
        return RestHighLevelClient(
            RestClient.builder(HttpHost.create(endpoint))
                .setHttpClientConfigCallback { hacb ->
                    hacb.addInterceptorLast(
                        interceptor
                    )
                })
    }

    open fun provideSearchRepository(client: RestHighLevelClient): SearchRepository = ElasticSearchRepository(client)

    open fun provideSearchUseCase(repository: SearchRepository) = SearchUseCase(repository)

    open fun provideFirebaseTokenVerifier(): FirebaseTokenVerifier = FirebaseAuthImpl(FirebaseAuth.getInstance())
}

class FirebaseAuthImpl(private val auth: FirebaseAuth) : FirebaseTokenVerifier {
    override fun verifyIdToken(token: String): Token {
        return auth.verifyIdToken(token).toToken()
    }
}

const val BYPASS_TOKEN = "123454321qwertyytrewq"
class AllowByPassTokenAuth(private val original: FirebaseTokenVerifier) : FirebaseTokenVerifier {
    override fun verifyIdToken(token: String): Token {
        if (token == BYPASS_TOKEN) {
            return Token("uid", "local", "Fake User", null, null, false, mapOf("sub" to "uid"))
        }
        return original.verifyIdToken(token)
    }
}

fun FirebaseToken.toToken() = Token(uid, issuer, name, picture, email, isEmailVerified, claims)
