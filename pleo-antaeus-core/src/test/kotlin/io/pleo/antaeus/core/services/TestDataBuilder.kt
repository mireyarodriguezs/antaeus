package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*
import java.math.BigDecimal
import java.sql.Date
import kotlin.random.Random

class TestDataBuilder {
    companion object {
        val pendingInvoices = setOf<Invoice>(
            Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PENDING, Date.valueOf("2022-03-01")),
            Invoice(2, 1, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PENDING, Date.valueOf("2022-03-01")),
            Invoice(3, 2, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PENDING, Date.valueOf("2022-03-01")),
            Invoice(4, 3, Money(BigDecimal.valueOf(500), Currency.EUR), InvoiceStatus.PENDING, Date.valueOf("2022-03-01")),
            Invoice(5, 4, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PENDING, Date.valueOf("2022-03-01"))
        ).toList()

        val allPaidInvoices = setOf<Invoice>(
            Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PAID, Date.valueOf("2022-03-01")),
            Invoice(2, 1, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PAID, Date.valueOf("2022-03-01")),
            Invoice(3, 2, Money(BigDecimal.valueOf(100000), Currency.DKK), InvoiceStatus.PAID, Date.valueOf("2022-03-01")),
            Invoice(4, 3, Money(BigDecimal.valueOf(500), Currency.EUR), InvoiceStatus.PAID, Date.valueOf("2022-03-01")),
            Invoice(5, 4, Money(BigDecimal.valueOf(50000), Currency.DKK), InvoiceStatus.PAID, Date.valueOf("2022-03-01"))
        ).toList()

        val testCustomers = setOf<Customer>(
            Customer(1, Currency.DKK, SubscriptionStatus.ACTIVE),
            Customer(2, Currency.DKK, SubscriptionStatus.ACTIVE),
            Customer(3, Currency.DKK, SubscriptionStatus.ACTIVE),
            Customer(4, Currency.EUR, SubscriptionStatus.ACTIVE),
            Customer(5, Currency.DKK, SubscriptionStatus.ACTIVE)
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
