package io.pleo.antaeus.messageconsumer

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageConsumer
import io.pleo.antaeus.core.services.BillingService
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val queueName = "PendingInvoicesQueue"
private const val consumerTag = "SingleConsumer"

class MessageConsumer(private val billingService: BillingService) {

    fun listen() {
        CoroutineFactory.create(nThreads = 4, block = {
            println("Creating message consumer....")
            val messageConsumer = QueueMessageConsumer(queueName, consumerTag)
            val deliverCallback = { messageStr: String? ->
                println("Received new message with content $messageStr")
                val idList = Json.decodeFromString<List<Int>>(messageStr.toString())
                runBlocking() {
                    billingService.settleForIds(idList.toList())
                }
            }

            val cancelCallback = { consumerTag: String? ->
                println("[$consumerTag] was canceled")
            }

            messageConsumer.listen(deliverCallback, cancelCallback)
        })
    }
}
