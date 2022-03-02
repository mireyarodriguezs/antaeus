package io.pleo.antaeus.scheduler

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageProducer
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.messaging.QueueNames
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.models.InvoiceStatus

private const val frequency = "*/10 * * * *"//"0 0 1 * *"
private const val batchSize = 10
private const val exchangeName = "exchangeName"

class FirstOfMonthBillingScheduler(private val billingService : BillingService, private val customerService: CustomerService) {

    fun start() {
        CoroutineFactory.createScheduledTask(nThreads = 4, frequency = frequency,  block = {
            val messageProducer = QueueMessageProducer()

            billingService.getByStatus(InvoiceStatus.PENDING)
                .filter { invoice -> customerService.hasActiveSubscription(invoice.customerId) }
                .map { invoice -> invoice.id }
                .chunked(batchSize)
                .map { batch ->
                    messageProducer.sendMessage(batch, exchangeName, QueueNames.pendingInvoicesQueueName)
                }
        })
    }
}