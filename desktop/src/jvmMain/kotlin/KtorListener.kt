import com.programmersbox.common.KtorPluginProvider
import io.ktor.client.HttpClientConfig
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ro.cosminmihu.ktor.monitor.ContentLength
import ro.cosminmihu.ktor.monitor.KtorMonitorLogging
import ro.cosminmihu.ktor.monitor.RetentionPeriod

val debugModule = module {
    single(named("monitor")) { KtorMonitorPlugin() } bind KtorPluginProvider::class
}

class KtorMonitorPlugin : KtorPluginProvider {
    override fun install(config: HttpClientConfig<*>) {
        config.install(KtorMonitorLogging) {
            sanitizeHeader { header -> header == "Authorization" }
            filter { request -> !request.url.host.contains("cosminmihu.ro") }
            showNotification = true
            retentionPeriod = RetentionPeriod.OneHour
            maxContentLength = ContentLength.Default
        }
    }
}