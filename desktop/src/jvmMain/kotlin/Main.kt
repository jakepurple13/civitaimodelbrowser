import androidx.compose.ui.window.application
import com.programmersbox.common.Network
import com.programmersbox.common.UIShow
import kotlinx.coroutines.runBlocking
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() = application {
    WindowWithBar(onCloseRequest = ::exitApplication) {
        UIShow(
            onShareClick = { link ->
                Toolkit.getDefaultToolkit().also {
                    it
                        .systemClipboard
                        .setContents(StringSelection(link), null)
                    it.beep()
                }
            }
        )
    }
}

fun main1(): Unit = runBlocking {
    val n = Network()
    n.getModels(1)
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }
}