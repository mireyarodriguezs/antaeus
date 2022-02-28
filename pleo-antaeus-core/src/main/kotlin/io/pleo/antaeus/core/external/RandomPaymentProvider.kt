package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Invoice
import kotlin.random.Random

class RandomPaymentProvider {
    companion object{
        // This is the mocked instance of the payment provider
        fun getPaymentProvider(): PaymentProvider {
            return object : PaymentProvider {
                override fun charge(invoice: Invoice): Boolean {
                    return Random.nextBoolean()
                }
            }
        }
    }
}