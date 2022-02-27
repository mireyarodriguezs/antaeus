package io.pleo.antaeus.core.external

import io.pleo.antaeus.core.RetryPolicy
import io.pleo.antaeus.models.Invoice
import kotlinx.coroutines.flow.*
import java.util.logging.Logger

class PaymentFacade(private val paymentProvider: PaymentProvider) {

    companion object {
        val LOG: Logger = Logger.getLogger(PaymentFacade::class.java.name)
    }

    suspend fun charge(invoice: Invoice): Boolean {
        with(RetryPolicy()){
            var result = false
            flow {
                emit(paymentProvider.charge(invoice))
            }.polly(numberRetries = 3)
            .catch { e ->
                LOG.severe("Failed processing invoice ${invoice.id}" +
                            "for customer ${invoice.customerId}. Manual intervention required." +
                            "Exception info ${e.message}")
            }.collect {
                value -> result = value
            }

            return result
        }
    }
}