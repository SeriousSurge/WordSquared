import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.hiremarknolan.wsq.App
import com.hiremarknolan.wsq.PlatformSettings

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("Word Squared") {
        App(PlatformSettings())
    }
} 