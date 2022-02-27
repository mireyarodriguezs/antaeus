package io.pleo.antaeus.core

import io.pleo.antaeus.core.exceptions.NetworkException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.logging.Logger

class RetryPolicy {
    companion object {
        val logger: Logger = Logger.getLogger(RetryPolicy::class.java.name)
        const val delayFactor = 2
    }
    
    fun <T> Flow<T>.polly(numberRetries: Int): Flow<T> {
        var currentDelay = 1000L
        return retryWhen { cause, attempt ->
            if (cause is NetworkException && attempt < numberRetries) {
                logger.warning("Transient network error. Will be retried")
                delay(currentDelay)
                currentDelay *= delayFactor
                return@retryWhen true
            } else {
                return@retryWhen false
            }
        }
    }
}