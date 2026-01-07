package com.programmersbox.common

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance

actual fun logToFirebase(message: Any) {
    runCatching { Firebase.crashlytics.log(message.toString()) }
        .onFailure { println(message) }
}

actual fun analyticsEvent(name: String, params: Map<String, Any>) {
    runCatching {
        Firebase.analytics.logEvent(name) {
            params.forEach { (k, v) -> param(k, v.toString()) }
        }
    }.onFailure { println("$name, Params: $params") }
}

actual suspend inline fun performanceTrace(name: String, crossinline block: suspend () -> Unit) {
    val trace = runCatching { Firebase.performance.newTrace(name) }
        .getOrNull()
    trace?.start()
    block()
    trace?.stop()
}