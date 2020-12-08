package com.nicolasmilliard.socialcats.searchapi.routes

import com.nicolasmilliard.socialcats.search.SearchUseCase
import com.nicolasmilliard.socialcats.searchapi.FirebaseAuthKey
import com.nicolasmilliard.socialcats.searchapi.PrincipalToken
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.search(searchUseCase: SearchUseCase) = authenticate(FirebaseAuthKey) {
    get("/v1/search") {
        val input = call.request.queryParameters["input"] ?: ""
        val uid = call.authentication.principal<PrincipalToken>()!!.value.uid
        val searchUsers = searchUseCase.searchUsers(uid, input)
        call.respond(searchUsers)
    }
}
