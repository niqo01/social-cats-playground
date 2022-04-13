import me.niqo.common.App
import me.niqo.common.AppTheme

0import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {

    renderComposable(rootElementId = "root") {
        Div({ style { padding(25.px) } }) {
            AppTheme {
                App()
            }
        }
    }
}

