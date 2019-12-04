package com.nicolasmilliard.socialcats.searchapi.routes

import com.nicolasmilliard.socialcats.search.SearchUseCase
import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.locations.Location
import io.ktor.response.cacheControl
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Location("/search")
data class Query(val input: String, val page: Int = 1, val count: Int = 20)

// fun Route.dummySearch(searchUseCase: SearchUseCase) {
//    get<Query> {
//        withContext(Dispatchers.IO) {
//            searchUseCase.searchUser(it.input)
//        }
//        call.respondText("Search ${it.input}", contentType = ContentType.Text.Plain)
//    }
// }

fun Route.dummySearch2(searchUseCase: SearchUseCase) {
    get("/search2") {
        val input = call.request.queryParameters["input"] ?: ""
        withContext(Dispatchers.IO) {
            val searchUsers = searchUseCase.searchUsers(null, input)
            call.response.cacheControl(CacheControl.MaxAge(maxAgeSeconds = 60, visibility = CacheControl.Visibility.Private))
            call.respond(searchUsers)
        }
    }
}
