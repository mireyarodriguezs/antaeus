package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import java.math.BigDecimal
import kotlin.random.Random

class TestDataBuilder {
    companion object {
        val pendingInvoices = setOf<Invoice>(
            Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PENDING),
            Invoice(2, 1, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PENDING),
            Invoice(3, 2, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PENDING),
            Invoice(4, 3, Money(BigDecimal.valueOf(500), Currency.EUR), InvoiceStatus.PENDING),
            Invoice(5, 4, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PENDING)
        ).toList()

        val allPaidInvoices = setOf<Invoice>(
            Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PAID),
            Invoice(2, 1, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PAID),
            Invoice(3, 2, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PAID),
            Invoice(4, 3, Money(BigDecimal.valueOf(500), Currency.EUR), InvoiceStatus.PAID),
            Invoice(5, 4, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PAID)
        ).toList()

        val testCustomers = setOf<Customer>(
            Customer(1, Currency.DKK),
            Customer(2, Currency.DKK),
            Customer(3, Currency.DKK),
            Customer(4, Currency.EUR),
            Customer(5, Currency.DKK)
        ).toList()
    }

    // This is the mocked instance of the payment provider
    internal fun getPaymentProvider(): PaymentProvider {
        return object : PaymentProvider {
            override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
            }
        }
    }
}
