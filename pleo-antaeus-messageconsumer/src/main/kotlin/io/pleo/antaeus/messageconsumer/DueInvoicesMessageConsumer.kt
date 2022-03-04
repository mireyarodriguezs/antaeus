package io.pleo.antaeus.messageconsumer

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.exceptions.EntityNotFoundException
import io.pleo.antaeus.core.messaging.QueueMessageConsumer
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.messaging.QueueNames
import io.pleo.antaeus.models.SubscriptionStatus
import io.pleo.antaeus.models.isPastGracePeriod
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.logging.Logger

class DueInvoicesMessageConsumer (
    private val billingService: BillingService,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService) {

    companion object {
        val LOG: Logger = Logger.getLogger(DueInvoicesMessageConsumer::class.java.name)
    }

    fun listen() {
        CoroutineFactory.create(nThreads = 4, block = {
            println("Creating DueInvoicesMessageConsumer....")
            val messageConsumer = QueueMessageConsumer(QueueNames.DueInvoicesQueue, QueueNames.DueInvoicesConsumerTag)
            val deliverCallback = { messageStr: String? ->
                println("DueInvoicesMessageConsumer Received new message with content $messageStr")
                val invoiceId = Json.decodeFromString<Int>(messageStr.toString())
                runBlocking {
                    processInvoice(invoiceId)
                }
            }

            val cancelCallback = { consumerTag: String? ->
                println("[$consumerTag] was canceled")
            }

            messageConsumer.listen(deliverCallback, cancelCallback)
        })
    }

    internal suspend fun processInvoice(invoiceId: Int){
        try {
            val invoice = invoiceService.fetch(invoiceId)
            val customer = customerService.fetch(invoice.customerId) // we make sure the customer exists

            val success = billingService.settleForId(invoiceId)
            if (!success && invoice.isPastGracePeriod()) {
                customerService.changeSubscriptionStatus(customer.id, SubscriptionStatus.INACTIVE)
            }

        } catch(exception: EntityNotFoundException){
            LOG.severe(exception.message)
        }
    }
}