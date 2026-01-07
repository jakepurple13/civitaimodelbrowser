package com.programmersbox.common

expect fun logToFirebase(message: Any)
expect fun analyticsEvent(name: String, params: Map<String, Any> = emptyMap())
expect suspend inline fun performanceTrace(name: String, crossinline block: suspend () -> Unit)