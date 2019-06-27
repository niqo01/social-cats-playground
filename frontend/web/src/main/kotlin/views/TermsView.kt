package views

import react.RBuilder
import react.ReactElement
import react.dom.article
import react.dom.div
import react.dom.h2
import react.dom.p
import react.dom.section

fun RBuilder.termsView(): ReactElement? = div(classes = "main") {
    section {

        h2(classes = "mdc-typography--headline2") {
            +"Terms of Use"
        }
        article {
            p(classes = "mdc-typography--body1") { +"This is terms of use" }
        }
    }
}
