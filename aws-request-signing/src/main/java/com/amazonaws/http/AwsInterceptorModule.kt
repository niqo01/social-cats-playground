package com.nicolasmilliard.socialcats.data

import com.amazonaws.http.AwsRequestSigningApacheInterceptor
import org.apache.http.HttpRequestInterceptor
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.auth.signer.Aws4Signer
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams
import software.amazon.awssdk.regions.Region

object AwsInterceptorModule {

    fun provideAwsInterceptor(serviceName: String, region: String): HttpRequestInterceptor {
        val credentialsProvider = EnvironmentVariableCredentialsProvider.create()
        val signer = Aws4Signer.create()
        val aws4SignerParams = Aws4SignerParams.builder()
            .signingName(serviceName)
            .signingRegion(Region.of(region))
            .awsCredentials(credentialsProvider.resolveCredentials()).build()
        return AwsRequestSigningApacheInterceptor(aws4SignerParams, signer)
    }
}
