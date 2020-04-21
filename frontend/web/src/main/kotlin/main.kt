import components.TermsComponent
import components.home
import kotlin.browser.document
import kotlin.browser.window
import react.dom.render
import react.router.dom.hashRouter
import react.router.dom.route
import react.router.dom.switch

fun main() {

    initFirebase()

    val auth = firebase.auth()

    window.onload = {
        render(document.getElementById("root")!!) {
            hashRouter {
                switch {
                    route("/", exact = true) { home(auth) }
                    route("/terms", TermsComponent::class)
                }
            }
        }
    }
}

fun initFirebase() {
    val config = jsObject {
        apiKey = "AIzaSyDkyTOOz32Ur9K4MydfRKRYe-l1UTgyIfI"
        authDomain = "sweat-monkey.firebaseapp.com"
        databaseURL = "https=//sweat-monkey.firebaseio.com"
        projectId = "sweat-monkey"
        storageBucket = "sweat-monkey.appspot.com"
        messagingSenderId = "112402926257"
        appId = "1:112402926257:web:7c9f1aa5b964610b"
    }

    firebase.initializeApp(config)
}
