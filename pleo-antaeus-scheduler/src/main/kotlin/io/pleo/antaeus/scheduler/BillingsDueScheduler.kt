package io.pleo.antaeus.scheduler

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageProducer
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.messaging.QueueNames
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.models.InvoiceStatus

private const val frequency = "*/10 * * * *"//"0 0 * * 0" // At 00.00 on Sundays
private const val exchangeName = "BillingsDueSchedulerExchangeName"

class BillingsDueScheduler (private val billingService : BillingService, private val customerService: CustomerService) {
    fun start() {
        CoroutineFactory.createScheduledTask(nThreads = 4, frequency, block = {
            val messageProducer = QueueMessageProducer()
            billingService.getByStatus(InvoiceStatus.NOTSUFFICIENTFUNDS)
                .filter { invoice -> customerService.hasActiveSubscription(invoice.customerId) }
                .map { invoice ->
                    messageProducer.sendMessage(invoice.id, exchangeName, QueueNames.DueInvoicesQueue)
                }
        })
    }
}