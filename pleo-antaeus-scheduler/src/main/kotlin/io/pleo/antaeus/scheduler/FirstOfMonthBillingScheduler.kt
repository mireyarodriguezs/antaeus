package io.pleo.antaeus.scheduler

import kotlinx.coroutines.*
import dev.inmo.krontab.doInfinityTz
import io.pleo.antaeus.core.CoroutineFactory
import io.pleo.antaeus.core.messaging.QueueMessageProducer
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.models.InvoiceStatus
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val frequency = "*/10 * * * *"//"0 0 1 * *"
private const val batchSize = 10
private const val queueName = "PendingInvoicesQueue"
private const val exchangeName = "exchangeName"

class BillingScheduler(val billingService : BillingService) {

    fun start() {
        CoroutineFactory.createScheduledTask(nThreads = 4, frequency = frequency,  block = {
            val messageProducer = QueueMessageProducer()

            //billingService.settleAllUnpaid()
            billingService.getByStatus(InvoiceStatus.PENDING)
                .chunked(batchSize)
                .map { batch ->
                    messageProducer.sendMessage(batch, exchangeName, queueName)
                }
        })
    }
}