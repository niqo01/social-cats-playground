package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment

const val trustedAccountId = "__Trusted__Account__"
const val prodAccountId = "__Prod__Account__"
const val preProdAccountId = "__Pre__Prod__Account__"
const val region = "__Region__"


fun main() {
    val app = App()

//    RequiredResourcesStack(app, "CdkRequiredResourcesStack", object : RequiredResourcesStackProps {
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

    CdkRequiredResourcesStack(app, "CdkRequiredResourcesStack", object : RequiredResourcesStackProps {
        override fun getEnv(): Environment {
            return Environment.builder()
                .account(prodAccountId)
                .region(region)
                .build()
        }

        override val trustedAccount: String
            get() = trustedAccountId

    })
    app.synth()
}
