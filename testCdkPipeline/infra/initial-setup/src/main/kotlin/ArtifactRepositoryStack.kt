package com.nicolasmilliard.testcdkpipeline

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.constructs.Construct
import software.amazon.awscdk.services.codeartifact.CfnDomain
import software.amazon.awscdk.services.codeartifact.CfnRepository
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.User

class ArtifactRepositoryStack(scope: Construct, id: String, props: StackProps) :
        Stack(scope, id, props) {

    init {
        val domain = CfnDomain.Builder.create(this, "Domain")
            .domainName("test-domain")
            .build()

        val repository = CfnRepository.Builder.create(this, "Repository")
            .repositoryName("test-repository")
            .domainName(domain.domainName)
            .externalConnections(listOf("public:maven-central"))
            .build()

        val user = User.Builder.create(this, "GithubUser").userName("github_user").build()

        val getServiceBearerPolicy = PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(
                listOf(
                    "sts:GetServiceBearerToken"
                )
            )
            .resources(listOf("*"))
            .conditions(mapOf("StringEquals" to mapOf("sts:AWSServiceName" to "codeartifact.amazonaws.com")))
            .build()

        val artifactPublishPolicy = PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(listOf(
                "codeartifact:GetAuthorizationToken",
                "codeartifact:ReadFromRepository",
                "codeartifact:PublishPackageVersion",
                "codeartifact:PutPackageMetadata",
                "codeartifact:DescribePackageVersion"
            ))
            .resources(listOf("*"))
            .build()


        user.addToPolicy(PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(listOf("codeartifact:GetRepositoryEndpoint"))
            .resources(listOf(repository.attrArn))
            .build())
        user.addToPolicy(artifactPublishPolicy)
        user.addToPolicy(getServiceBearerPolicy)
    }
}