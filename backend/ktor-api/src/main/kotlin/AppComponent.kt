package com.nicolasmilliard.socialcats.searchapi

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.cloud.FirestoreClient
import com.nicolasmilliard.socialcats.data.AwsInterceptorModule
import com.nicolasmilliard.socialcats.data.ElasticServiceInterceptorModule
import com.nicolasmilliard.socialcats.payment.PaymentProcessor
import com.nicolasmilliard.socialcats.payment.Payments
import com.nicolasmilliard.socialcats.payment.StripeProcessor
import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.search.repository.ElasticSearchRepository
import com.nicolasmilliard.socialcats.search.repository.SearchRepository
import com.nicolasmilliard.socialcats.store.RealUserStoreAdmin
import com.nicolasmilliard.socialcats.store.UserStoreAdmin
import io.ktor.config.ApplicationConfig
import org.apache.http.HttpHost
import org.apache.http.HttpRequestInterceptor
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun getModules(config: ApplicationConfig): List<Module> {

    val isProduction = config
        .property("env.isProduction").getString() == "true"
    val useAwsEs = config
        .property("elasticSearch.useAws").getString() == "true"
    val mods = ArrayList<Module>(6)
    mods.add(module { single { config } })

    mods.add(storeModule)
    mods.add(paymentModule)
    mods.add(searchModule)

    when {
        isProduction -> {
            mods.add(authModule)
        }
        else -> {
            mods.add(allowByPassAuthModule)
        }
    }

    if (useAwsEs) {
        mods.add(awsModule)
    } else {
        mods.add(esModule)
    }

    return mods
}

val awsModule = module {
    single {
        val config: ApplicationConfig = get()
        val serviceName: String = config.property("elasticSearch.serviceName").getString()
        val region: String = config.property("elasticSearch.region").getString()
        AwsInterceptorModule.provideAwsInterceptor(serviceName, region)
    }
}

val esModule = module {
    single {
        val config: ApplicationConfig = get()
        val id = config.property("elasticSearch.apiKeyId").getString()
        val apiKey = config.property("elasticSearch.apiKey").getString()
        ElasticServiceInterceptorModule.provideElasticServiceInterceptor(id, apiKey)
    }
}

val authModule = module {
    single<FirebaseTokenVerifier> {
        FirebaseAuthImpl(FirebaseAuth.getInstance())
    }
}

val allowByPassAuthModule = module {
    single<FirebaseTokenVerifier> {
        AllowByPassTokenAuth(FirebaseAuthImpl(FirebaseAuth.getInstance()))
    }
}

val searchModule = module {
    single {
        val config: ApplicationConfig = get()
        val endpoint: String = config.property("elasticSearch.endpoint").getString()
        val interceptor: HttpRequestInterceptor = get()
        RestHighLevelClient(
            RestClient.builder(HttpHost.create(endpoint))
                .setHttpClientConfigCallback { hacb ->
                    hacb.addInterceptorLast(
                        interceptor
                    )
                })
    }

    single<SearchRepository> {
        ElasticSearchRepository(get())
    }

    single {
        SearchUseCase(get())
    }
}

val storeModule = module {
    single {
        FirestoreClient.getFirestore()
    }
    single<UserStoreAdmin> {
        RealUserStoreAdmin(get())
    }
}

val paymentModule = module {
    single<PaymentProcessor> {
        val config: ApplicationConfig = get()
        val stripePKey: String = config.property("stripe.pKey").getString()
        val stripeSKey: String = config.property("stripe.sKey").getString()
        StripeProcessor(stripePKey, stripeSKey)
    }
    single {
        Payments(get(), get())
    }
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
