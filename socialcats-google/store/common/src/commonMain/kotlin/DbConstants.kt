package com.nicolasmilliard.socialcats.store

object DbConstants {
    object Collections {
        object Users {
            const val NAME = "users"

            object Fields {
                const val NAME = "name"
                const val CREATED_AT = "createdAt"
                const val PHONE_NUMBER = "phoneNumber"
                const val PHOTO_URL = "photoUrl"
                const val EMAIL = "email"
                const val EMAIL_VERIFIED = "emailVerified"
                const val MEMBERSHIP_STATUS = "membershipStatus"
            }

            object PaymentProcessor {
                const val NAME = "paymentProcessor"

                enum class Processor(val id: String) {
                    STRIPE("stripe"),
                }

                object Fields {
                    const val CUSTOMER_ID = "customerId"
                }
            }
        }

        object InstanceIds {
            const val name = "instanceIds"

            object Fields {
                const val TOKEN = "token"
                const val LANGUAGE_TAG = "languageTag"
            }
        }
    }
}
