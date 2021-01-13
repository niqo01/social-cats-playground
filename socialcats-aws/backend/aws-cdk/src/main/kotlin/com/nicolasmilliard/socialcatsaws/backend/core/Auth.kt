package com.nicolasmilliard.socialcatsaws.backend.core

import software.amazon.awscdk.core.CfnOutput
import software.amazon.awscdk.core.CfnOutputProps
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.services.cognito.AccountRecovery
import software.amazon.awscdk.services.cognito.AuthFlow
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs
import software.amazon.awscdk.services.cognito.CfnIdentityPool
import software.amazon.awscdk.services.cognito.CfnIdentityPoolRoleAttachment
import software.amazon.awscdk.services.cognito.CognitoDomainOptions
import software.amazon.awscdk.services.cognito.Mfa
import software.amazon.awscdk.services.cognito.MfaSecondFactor
import software.amazon.awscdk.services.cognito.OAuthFlows
import software.amazon.awscdk.services.cognito.OAuthSettings
import software.amazon.awscdk.services.cognito.PasswordPolicy
import software.amazon.awscdk.services.cognito.SignInAliases
import software.amazon.awscdk.services.cognito.StandardAttribute
import software.amazon.awscdk.services.cognito.StandardAttributes
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.cognito.UserPoolClient
import software.amazon.awscdk.services.cognito.UserPoolClientIdentityProvider
import software.amazon.awscdk.services.cognito.UserPoolClientOptions
import software.amazon.awscdk.services.cognito.UserPoolDomainOptions
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.FederatedPrincipal
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.Role

class Auth constructor(
  scope: Construct,
  id: String,
  isProd: Boolean
) :
  Construct(scope, id) {

  val userPool: UserPool
  val androidClient: UserPoolClient
  val identityPool: CfnIdentityPool

  init {
    userPool = createCognito(isProd)
    androidClient = createAndroidClient(isProd, userPool)
    identityPool = createIdentityPool(userPool, androidClient)
  }

  private fun createCognito(isProd: Boolean): UserPool {
    val pool = UserPool.Builder.create(this, "userPool")
      .userPoolName("socialcats-userpool")
      .selfSignUpEnabled(true)
      .signInAliases(
        SignInAliases.builder()
          .email(true)
          .build()
      )
      .autoVerify(AutoVerifiedAttrs.builder().email(true).build())
      .mfa(Mfa.OPTIONAL)
      .mfaSecondFactor(
        MfaSecondFactor.builder()
          .sms(true)
          .otp(false)
          .build()
      )
      .passwordPolicy(
        PasswordPolicy.builder()
          .minLength(6)
          .build()
      )
      .accountRecovery(AccountRecovery.EMAIL_AND_PHONE_WITHOUT_MFA)
      .standardAttributes(
        StandardAttributes.builder()
          .email(
            StandardAttribute.builder()
              .required(true)
              .mutable(false)
              .build()
          )
          .build()
      )
      .build()

    CfnOutput(
      this,
      "CognitoUserPoolIdOutput",
      CfnOutputProps.builder()
        .value(pool.userPoolId)
        .description("Cognito User Pool Id")
        .build()
    )
    CfnOutput(
      this,
      "CognitoUserPoolProviderUrlOutput",
      CfnOutputProps.builder()
        .value(pool.userPoolProviderUrl)
        .description("Cognito User Pool provider url")
        .build()
    )

    val addDomain = pool.addDomain(
      "domain",
      UserPoolDomainOptions.builder()
        .cognitoDomain(
          CognitoDomainOptions.builder()
            .domainPrefix("social-cats")
            .build()
        )
        .build()
    )

    CfnOutput(
      this,
      "CognitoUserPoolDomainOutput",
      CfnOutputProps.builder()
        .value(addDomain.domainName)
        .description("Cognito User Pool domain")
        .build()
    )

    return pool
  }

  private fun createIdentityPool(pool: UserPool, androidClient: UserPoolClient): CfnIdentityPool {
    val identityPool = CfnIdentityPool.Builder.create(this, "identitypool")
      .allowUnauthenticatedIdentities(true)
      .cognitoIdentityProviders(
        listOf(
          CfnIdentityPool.CognitoIdentityProviderProperty.Builder()
            .clientId(androidClient.userPoolClientId)
            .providerName(pool.userPoolProviderName)
            .build()
        )
      )
      .build()

    CfnOutput(
      this,
      "IdentityPoolRefOutput",
      CfnOutputProps.builder()
        .value(identityPool.ref)
        .description("Cognito Identity Pool Ref")
        .build()
    )
    CfnOutput(
      this,
      "IdentityPoolLogicalIdOutput",
      CfnOutputProps.builder()
        .value(identityPool.logicalId)
        .description("Cognito Identity Pool Logical Id")
        .build()
    )

    val unAuthRole = Role.Builder.create(this, "unauthenticated-role")
      .assumedBy(
        FederatedPrincipal(
          "cognito-identity.amazonaws.com",
          mapOf(
            "StringEquals" to hashMapOf("cognito-identity.amazonaws.com:aud" to identityPool.ref),
            "ForAnyValue:StringLike" to hashMapOf("cognito-identity.amazonaws.com:amr" to "unauthenticated")
          ),
          "sts:AssumeRoleWithWebIdentity"
        )
      )
      .build()
    unAuthRole.addToPolicy(
      PolicyStatement.Builder.create()
        .effect(Effect.ALLOW)
        .actions(
          listOf(
            "mobileanalytics:PutEvents",
            "cognito-sync:*"
          )
        )
        .resources(listOf("*"))
        .build()
    )

    val authRole = Role.Builder.create(this, "authenticated-role")
      .assumedBy(
        FederatedPrincipal(
          "cognito-identity.amazonaws.com",
          mapOf(
            "StringEquals" to hashMapOf("cognito-identity.amazonaws.com:aud" to identityPool.ref),
            "ForAnyValue:StringLike" to hashMapOf("cognito-identity.amazonaws.com:amr" to "authenticated")
          ),
          "sts:AssumeRoleWithWebIdentity"
        )
      )
      .build()
    authRole.addToPolicy(
      PolicyStatement.Builder.create()
        .effect(Effect.ALLOW)
        .actions(
          listOf(
            "mobileanalytics:PutEvents",
            "cognito-sync:*",
            "cognito-identity:*"
          )
        )
        .resources(listOf("*"))
        .build()
    )

    CfnIdentityPoolRoleAttachment.Builder.create(this, "DefaultIdentityRole")
      .identityPoolId(identityPool.ref)
      .roles(
        mapOf(
          "unauthenticated" to unAuthRole.roleArn,
          "authenticated" to authRole.roleArn
        )
      )
      .build()

    return identityPool
  }

  private fun createAndroidClient(isProd: Boolean, userPool: UserPool): UserPoolClient {
    val client = userPool.addClient(
      "android-client",
      UserPoolClientOptions.builder()
        .oAuth(
          OAuthSettings.builder()
            .flows(
              OAuthFlows.builder()
                .authorizationCodeGrant(true)
                .implicitCodeGrant(!isProd)
                .build()
            )
            .callbackUrls(listOf("socialcats://home"))
            .logoutUrls(listOf("socialcats://home?sign_out=true"))
            .build()
        )
        .authFlows(
          AuthFlow.builder()
            .userPassword(true)
            .userSrp(true)
            .build()
        )
        .preventUserExistenceErrors(true)
        .supportedIdentityProviders(listOf(UserPoolClientIdentityProvider.COGNITO))
        .build()
    )
    CfnOutput(
      this,
      "UserPoolAndroidClientIdOutput",
      CfnOutputProps.builder()
        .value(client.userPoolClientId)
        .description("Cognito User Pool client Id for Android")
        .build()
    )

    return client
  }
}
