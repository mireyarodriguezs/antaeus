package io.pleo.antaeus.messageconsumer

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageConsumer
import io.pleo.antaeus.core.services.BillingService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val queueName = "DueInvoicesQueue"
private const val consumerTag = "SingleConsumer"

class DueInvoicesMessageConsumer (
    private val billingService: BillingService) {

        fun listen() {
            CoroutineFactory.create(nThreads = 4, block = {
                println("Creating DueInvoicesMessageConsumer....")
                val messageConsumer = QueueMessageConsumer(queueName, consumerTag)
                val deliverCallback = { messageStr: String? ->
                    println("Received new message with content $messageStr")
                    val id = Json.decodeFromString<Int>(messageStr.toString())

                    // TODO: figure it out what is the best course of action when there is a customer
                    // That has consumed the grace period for its subscription
                }

                val cancelCallback = { consumerTag: String? ->
                    println("[$consumerTag] was canceled")
                }

                messageConsumer.listen(deliverCallback, cancelCallback)
            })
        }
}