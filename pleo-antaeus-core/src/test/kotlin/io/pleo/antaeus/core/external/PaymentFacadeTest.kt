package io.pleo.antaeus.core.external

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PaymentFacadeTest {

    private lateinit var paymentProvider: PaymentProvider
    private lateinit var paymentFacade: PaymentFacade

    private val someInvoice : Invoice =
        Invoice(1, 1, Money(BigDecimal.valueOf(1000), Currency.EUR), InvoiceStatus.PENDING)

    @Test
    fun `will not retry when no exception is happening`() = runBlocking {
        // Given a payment provider which always succeed
        paymentProvider = mockk {
            every { charge(ofType()) } returns true
        }

        paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
        // When
        paymentFacade.charge(someInvoice)
        // Then
        verify(exactly = 1) { paymentProvider.charge(someInvoice) }
    }

    @Test
    fun `will not retry when not retryable exception has happened`() = runBlocking {
        // Given a payment provider which always succeed
        paymentProvider = mockk {
            every { charge(ofType()) } throws InvoiceNotFoundException(1)
        }

        paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
        // When
        paymentFacade.charge(someInvoice)
        // Then
        verify(exactly = 1) { paymentProvider.charge(someInvoice) }
    }

    @Test
    fun `will retry when NetworkException has happened`() = runBlocking {
        // Given a payment provider which always succeed
        paymentProvider = mockk {
            every { charge(ofType()) } throws NetworkException()
        }

        paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
        // When
        paymentFacade.charge(someInvoice)
        // Then
        verify(exactly = 4) { paymentProvider.charge(someInvoice) }
    }
}