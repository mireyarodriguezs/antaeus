package io.pleo.antaeus.scheduler

import kotlinx.coroutines.*
import dev.inmo.krontab.doInfinityTz
import io.pleo.antaeus.core.services.BillingService
import kotlin.coroutines.CoroutineContext

private const val frequency = "*/10 * * * *"//"0 0 1 * *"

class BillingScheduler(val billingService : BillingService) {

    private val context: CoroutineContext = Job() + Dispatchers.Default
    private val scope: CoroutineScope = CoroutineScope(context)

    fun start() {
        scope.launch(context) {
            doInfinityTz(frequency) {
                println("Launching coroutine with frequency $frequency ....")
                billingService.settleAllUnpaid()
            }
        }
    }
}