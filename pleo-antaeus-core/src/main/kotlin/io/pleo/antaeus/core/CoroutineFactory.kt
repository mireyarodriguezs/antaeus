package io.pleo.antaeus.core

import dev.inmo.krontab.doInfinityTz
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class CoroutineFactory {
    companion object {
        fun createScheduledTask(nThreads : Int, frequency: String, block: ()-> Unit) {
            val threadPool = Executors.newFixedThreadPool(nThreads)
            val dispatcher = threadPool.asCoroutineDispatcher()
            val context: CoroutineContext = Job() + dispatcher
            val scope: CoroutineScope = CoroutineScope(context)

            scope.launch(context) {
                doInfinityTz(frequency) {
                    println("Launching scheduler with frequency $frequency ....")
                    block()
                }
            }.invokeOnCompletion {
                threadPool.shutdown()
            }
        }

        fun create(nThreads : Int, block: ()-> Unit) {
            val threadPool = Executors.newFixedThreadPool(nThreads)
            val dispatcher = threadPool.asCoroutineDispatcher()
            val context: CoroutineContext = Job() + dispatcher
            val scope: CoroutineScope = CoroutineScope(context)

            scope.launch(context) {
                println("Launching coroutine")
                block()
            }.invokeOnCompletion {
                threadPool.shutdown()
            }
        }
    }
}