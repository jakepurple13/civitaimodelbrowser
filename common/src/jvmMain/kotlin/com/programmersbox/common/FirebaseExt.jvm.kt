package com.programmersbox.common

actual fun logToFirebase(message: Any) {
    println(message)
}

actual fun analyticsEvent(name: String, params: Map<String, Any>) {
    println("Event: $name, Params: $params")
}

actual inline fun performanceTrace(name: String, crossinline block: () -> Unit) {
    block()
}