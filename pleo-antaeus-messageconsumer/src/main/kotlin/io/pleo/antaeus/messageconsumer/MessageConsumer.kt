package io.pleo.antaeus.messageconsumer

import io.pleo.antaeus.core.messaging.QueueMessageConsumer
import io.pleo.antaeus.core.services.BillingService
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val queueName = "PendingInvoicesQueue"
private const val consumerTag = "SingleConsumer"

class MessageConsumer(private val billingService: BillingService) {
    fun listen() {
        println("Launching coroutine for consumer....")
        val threadPool = Executors.newFixedThreadPool(4)
        val dispatcher = threadPool.asCoroutineDispatcher()
        val context: CoroutineContext = Job() + dispatcher
        val scope: CoroutineScope = CoroutineScope(context)

        scope.launch(context) {
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
        }.invokeOnCompletion {
            threadPool.shutdown()
        }
    }
}
