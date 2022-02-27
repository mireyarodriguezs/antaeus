package io.pleo.antaeus.core.external

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.models.Invoice
import java.util.logging.Logger

class PaymentFacade(private val paymentProvider: PaymentProvider) {

    companion object {
        val LOG = Logger.getLogger(PaymentFacade::class.java.name)
    }

    fun charge(invoice: Invoice) : Boolean {
        try {
            return paymentProvider.charge(invoice)
        } catch (network: NetworkException){
            // TODO: Add retry logic for transient errors
            LOG.warning("Transient network error. Will be retried")
        } catch (e: Exception){
            // rest of the possible exceptions seems to be not retriable, log exception for now
            LOG.severe("Exception while processing payment for invoice ${invoice.id}")
        }

        return false
    }
}