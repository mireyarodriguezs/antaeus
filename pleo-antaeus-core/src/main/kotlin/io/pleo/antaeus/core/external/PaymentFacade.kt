package io.pleo.antaeus.core.external

import io.pleo.antaeus.core.RetryPolicy
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.EntityNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.flow.*
import java.util.logging.Logger

class PaymentFacade(private val paymentProvider: PaymentProvider) {

    companion object {
        val LOG: Logger = Logger.getLogger(PaymentFacade::class.java.name)
    }

    suspend fun charge(invoice: Invoice): InvoiceStatus {
        with(RetryPolicy()){
            var result = invoice.status

            flow {
                emit(paymentProvider.charge(invoice))
            }.polly(numberRetries = 3)
            .catch { e ->
                when (e) {
                    is InvoiceNotFoundException -> result = InvoiceStatus.INVOICENOTFOUND
                    is CustomerNotFoundException -> result = InvoiceStatus.CUSTOMERNOTFOUND
                    is CurrencyMismatchException -> result = InvoiceStatus.CURRENCYMISMATCH
                }

                LOG.severe("Failed processing invoice ${invoice.id}" +
                            "for customer ${invoice.customerId}. Manual intervention required." +
                            "Exception info ${e.message}")
            }.collect {
                value ->
                    result = if (value) {
                        InvoiceStatus.PAID
                    } else {
                        InvoiceStatus.NOTSUFFICIENTFUNDS
                    }
            }

            return result
        }
    }
}