package io.pleo.antaeus.core.external

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.*
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

    @Test
    fun `will return CURRENCYMISMATCH status when payment provider fails with CurrencyMismatchException`()
        = runBlocking {
            // Given a payment provider which always throws CurrencyMismatchException
            paymentProvider = mockk {
                every { charge(ofType()) } throws CurrencyMismatchException(1, 1)
            }

            paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
            // When
            var result = paymentFacade.charge(someInvoice)
            // Then
            assert(result == InvoiceStatus.CURRENCYMISMATCH)
    }
    @Test
    fun `will return CUSTOMERNOTFOUND status when payment provider fails with CustomerNotFoundException`()
            = runBlocking {
        // Given a payment provider which always throws CustomerNotFoundException
        paymentProvider = mockk {
            every { charge(ofType()) } throws CustomerNotFoundException(1)
        }

        paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
        // When
        var result = paymentFacade.charge(someInvoice)
        // Then
        assert(result == InvoiceStatus.CUSTOMERNOTFOUND)
    }
    @Test
    fun `will return INVOICENOTFOUND status when payment provider fails with Invoice`()
            = runBlocking {
        // Given a payment provider which always throws InvoiceNotFoundException
        paymentProvider = mockk {
            every { charge(ofType()) } throws InvoiceNotFoundException(1)
        }

        paymentFacade = PaymentFacade(paymentProvider = paymentProvider)
        // When
        var result = paymentFacade.charge(someInvoice)
        // Then
        assert(result == InvoiceStatus.INVOICENOTFOUND)
    }
}