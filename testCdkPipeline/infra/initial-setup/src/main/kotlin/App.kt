package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps

const val trustedAccountId = "480917579245"
const val prodAccountId = "275972720939"
const val preProdAccountId = "480465344025"
const val region = "us-east-1"


fun main() {
    val app = App()

    ArtifactRepositoryStack(app, "ArtifactRepositoryStack", StackProps.builder()
        .env(Environment.builder()
            .account(trustedAccountId)
            .region(region)
            .build())
        .build())
//    CdkRequiredResourcesStack(app, "CdkRequiredResourcesStack", object : RequiredResourcesStackProps {
//        override fun getEnv(): Environment {
//            return Environment.builder()
//                .account(preProdAccountId)
//                .region(region)
//                .build()
//        }
//
//        override val trustedAccount: String
//            get() = trustedAccountId
//
//    })

//    CdkRequiredResourcesStack(app, "CdkRequiredResourcesStack", object : RequiredResourcesStackProps {
//        override fun getEnv(): Environment {
//            return Environment.builder()
//                .account(prodAccountId)
//                .region(region)
//                .build()
//        }
//
//        override val trustedAccount: String
//            get() = trustedAccountId
//
//    })
    app.synth()
}
