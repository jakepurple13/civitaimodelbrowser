package com.programmersbox.common

expect fun logToFirebase(message: Any)
expect fun analyticsEvent(name: String, params: Map<String, Any> = emptyMap())
expect inline fun performanceTrace(name: String, crossinline block: () -> Unit)