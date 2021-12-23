package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.Stage
import software.amazon.awscdk.StageProps
import software.constructs.Construct
import java.util.*

class AppStage(
    scope: Construct,
    id: String,
    props: StageProps,
    envName: String,
    isProd: Boolean,
    lambdaArtifacts: Properties
) : Stage(scope, id, props) {
    val result: SetupTasksResult

    init {

        result = setupStacks(envName, isProd, lambdaArtifacts)
    }
}