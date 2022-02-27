package io.pleo.antaeus.core.messaging

const val LocalQueueUserName = "guest"
const val LocalQueuePassword = "guest"
const val LocalQueueHostName = "localhost"
const val LocalQueuePortNumber = "5672"
const val LocalQueueVirtualHost = "/"

class LocalQueue() {
    companion object {
        fun getUri() : String {
            return "amqp://$LocalQueueUserName:$LocalQueuePassword@$LocalQueueHostName:$LocalQueuePortNumber$LocalQueueVirtualHost"
        }
    }
}