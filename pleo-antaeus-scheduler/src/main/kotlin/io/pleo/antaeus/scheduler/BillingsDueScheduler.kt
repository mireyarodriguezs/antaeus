package io.pleo.antaeus.scheduler

import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageProducer
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus

private const val frequency = "0 0 * * 0" // At 00.00 on Sundays
private const val queueName = "DueInvoicesQueue"
private const val exchangeName = "exchangeName"

class BillingsDueScheduler (private val billingService : BillingService){
    fun start() {
        CoroutineFactory.createScheduledTask(nThreads = 4, frequency, block = {
            val messageProducer = QueueMessageProducer()
            billingService.getByStatus(InvoiceStatus.NOTSUFFICIENTFUNDS)
                .map { batch ->
                    messageProducer.sendMessage(batch, exchangeName, queueName)
                }
        })
    }
}