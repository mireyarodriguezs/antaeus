package io.pleo.antaeus.messageconsumer

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.Date

class DueInvoiceMessageConsumerTest {

    private lateinit var customerService: CustomerService
    private lateinit var invoiceService: InvoiceService
    private lateinit var billingService : BillingService

    private val recentDueInvoice = Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK),
        InvoiceStatus.NOTSUFFICIENTFUNDS, Date.valueOf("2022-03-01"))
    private val pastGracePeriodInvoice = Invoice(1, 1, Money(BigDecimal.valueOf(100000), Currency.DKK),
        InvoiceStatus.NOTSUFFICIENTFUNDS, Date.valueOf("2022-01-01"))

    private val customers = setOf(
        Customer(1, Currency.DKK, SubscriptionStatus.ACTIVE)
    ).toList()

    @Test
    fun `will handle exception when invoice is not found`() = runBlocking {
        // Given
        invoiceService = mockk {
            every { fetch(1) } throws InvoiceNotFoundException(1)
        }
        customerService = mockk()
        billingService = mockk()
        val consumer = DueInvoicesMessageConsumer(billingService, invoiceService, customerService)
        // When
        consumer.processInvoice(invoiceId = 1)
        // Then we do nothing but we catch the exception
        verify (exactly = 0) { customerService.fetch(ofType()) }
        verify (exactly = 0) { runBlocking { billingService.settleForId(ofType())} }
        verify (exactly = 0) { customerService.changeSubscriptionStatus(ofType(), ofType()) }
    }

    @Test
    fun `will handle exception when customer is not found`() = runBlocking {
        // Given
        invoiceService = mockk {
            every { fetch(1) } returns pastGracePeriodInvoice
            every { updateInvoiceStatus(1, InvoiceStatus.NOTSUFFICIENTFUNDS)} returns true
        }
        customerService = mockk {
            every { fetch(1) } throws CustomerNotFoundException(1)
        }
        val paymentFacade = mockk<PaymentFacade>(){
            coEvery  { charge(ofType()) }  returns InvoiceStatus.NOTSUFFICIENTFUNDS
        }
        billingService = BillingService(invoiceService = invoiceService, paymentFacade = paymentFacade)
        val consumer = DueInvoicesMessageConsumer(billingService, invoiceService, customerService)

        // When
        consumer.processInvoice(invoiceId = 1)
        // Then we do nothing but we catch the exception
        verify (exactly = 1) { customerService.fetch(ofType()) }
        verify (exactly = 0) { customerService.changeSubscriptionStatus(ofType(), ofType()) }
    }

    @Test
    fun `will be able to settle when the payment is finally through`() = runBlocking {
        // Given
        invoiceService = mockk {
            every { fetch(1) } returns recentDueInvoice
            every { updateInvoiceStatus(1, InvoiceStatus.PAID)} returns true
        }
        customerService = mockk {
            every { fetch(1) } returns customers[0]
        }
        val paymentFacade = mockk<PaymentFacade>(){
            coEvery  { charge(ofType()) }  returns InvoiceStatus.PAID
        }

        billingService = BillingService(invoiceService = invoiceService, paymentFacade = paymentFacade)

        val consumer = DueInvoicesMessageConsumer(billingService, invoiceService, customerService)
        // When
        consumer.processInvoice(invoiceId = 1)
        // Then we do nothing but we catch the exception
        verify (exactly = 2) { invoiceService.fetch(1) }
        verify (exactly = 1) { invoiceService.updateInvoiceStatus(1, InvoiceStatus.PAID) }
        verify (exactly = 0) { customerService.changeSubscriptionStatus(ofType(), ofType()) }
    }

    @Test
    fun `will deactivate the customer if the grace period is past`() = runBlocking {
        // Given
        invoiceService = mockk {
            every { fetch(1) } returns pastGracePeriodInvoice
            every { updateInvoiceStatus(1, InvoiceStatus.NOTSUFFICIENTFUNDS)} returns true
        }
        customerService = mockk {
            every { fetch(1) } returns customers[0]
            every { changeSubscriptionStatus(1, SubscriptionStatus.INACTIVE) } returns Unit
        }
        val paymentFacade = mockk<PaymentFacade>(){
            coEvery  { charge(ofType()) }  returns InvoiceStatus.NOTSUFFICIENTFUNDS
        }

        billingService = BillingService(invoiceService = invoiceService, paymentFacade = paymentFacade)

        val consumer = DueInvoicesMessageConsumer(billingService, invoiceService, customerService)
        // When
        consumer.processInvoice(invoiceId = 1)
        // Then we do nothing but we catch the exception
        verify (exactly = 2) { invoiceService.fetch(1) }
        verify (exactly = 1) { invoiceService.updateInvoiceStatus(1, InvoiceStatus.NOTSUFFICIENTFUNDS) }
        verify (exactly = 1) { customerService.changeSubscriptionStatus(1, SubscriptionStatus.INACTIVE) }
    }
}