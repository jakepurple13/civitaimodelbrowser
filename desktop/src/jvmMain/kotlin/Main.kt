import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.programmersbox.common.Network
import com.programmersbox.common.UIShow
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.PreComposeWindow

fun main() = application {
    WindowWithBar(onCloseRequest = ::exitApplication) {
        UIShow()
    }
}

fun main1(): Unit = runBlocking {
    val n = Network()
    n.getModels(1)
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }
}