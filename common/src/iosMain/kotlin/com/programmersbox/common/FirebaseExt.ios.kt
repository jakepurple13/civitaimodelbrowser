package com.programmersbox.common

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.perf.performance

actual fun logToFirebase(message: Any) {
    runCatching { Firebase.crashlytics.log(message.toString()) }
}

actual fun analyticsEvent(name: String, params: Map<String, Any>) {
    runCatching {
        Firebase.analytics.logEvent(name) {
            params.forEach { (k, v) -> param(k, v.toString()) }
        }
    }
}

actual suspend inline fun performanceTrace(name: String, crossinline block: suspend () -> Unit) {
    val trace = runCatching { Firebase.performance.newTrace(name) }
        .getOrNull()
    trace?.start()
    block()
    trace?.stop()
}