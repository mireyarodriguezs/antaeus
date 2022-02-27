package io.pleo.antaeus.scheduler

import kotlinx.coroutines.*
import dev.inmo.krontab.doInfinityTz
import kotlin.coroutines.CoroutineContext

private const val frequency = "*/10 * * * *"//"0 0 1 * *"

class BillingScheduler() {

    private val context: CoroutineContext = Job() + Dispatchers.Default
    private val scope: CoroutineScope = CoroutineScope(context)

    fun Start() {
        scope.launch(context) {
            doInfinityTz(frequency) {
                println("Launching coroutine with frequency $frequency ....")
            }
        }
    }
}