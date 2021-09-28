package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.iam.AccountPrincipal

import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.RoleProps
import software.constructs.Construct

class CdkRequiredResourcesStack(scope: Construct, id: String, props: RequiredResourcesStackProps) :
    Stack(scope, id, props) {

    init {
        val readRole = Role(
            this, "ReadRole", RoleProps.builder()
                .assumedBy(AccountPrincipal(props.trustedAccount))
                .roleName("cdk-readOnlyRole")
                .build()
        )
        readRole.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("ReadOnlyAccess"))
    }
}

interface RequiredResourcesStackProps : StackProps {
    /**
     * The AWS Account ID to add to the IAM Role trust policy.
     * Any role from this AWS Account will be able to assume the
     * two roles created
     */
    val trustedAccount: String
}