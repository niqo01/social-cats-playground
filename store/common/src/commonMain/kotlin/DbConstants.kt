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
