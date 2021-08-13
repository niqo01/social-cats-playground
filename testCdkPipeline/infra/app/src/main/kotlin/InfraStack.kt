package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import kotlin.jvm.JvmOverloads
import software.constructs.Construct

class InfraStack @JvmOverloads constructor(scope: Construct?, id: String?, props: StackProps? = null) :
    Stack(scope, id, props)