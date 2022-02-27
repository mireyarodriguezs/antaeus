package io.pleo.antaeus.scheduler

import kotlinx.coroutines.*
import dev.inmo.krontab.doInfinityTz
import io.pleo.antaeus.core.messaging.QueueMessageProducer
import io.pleo.antaeus.core.services.BillingService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val frequency = "*/10 * * * *"//"0 0 1 * *"
private const val batchSize = 10
private const val queueName = "PendingInvoicesQueue"
private const val exchangeName = "exchangeName"

class BillingScheduler(val billingService : BillingService) {

    fun start() {
        // This function should be executing indefinitely, so we need to run it explicitly on its own thread
        // so the JVM knows about it and does not finish on the main thread.
        val threadPool = Executors.newFixedThreadPool(4)
        val dispatcher = threadPool.asCoroutineDispatcher()
        val context: CoroutineContext = Job() + dispatcher
        val scope: CoroutineScope = CoroutineScope(context)

        scope.launch(context) {
            doInfinityTz(frequency) {
                println("Launching coroutine with frequency $frequency ....")
                val messageProducer = QueueMessageProducer()

                //billingService.settleAllUnpaid()
                billingService.getAllPendingIds()
                    .chunked(batchSize)
                    .map { batch ->
                        messageProducer.sendMessage(batch, exchangeName, queueName)
                    }
            }
        }.invokeOnCompletion {
            threadPool.shutdown()
        }
    }
}