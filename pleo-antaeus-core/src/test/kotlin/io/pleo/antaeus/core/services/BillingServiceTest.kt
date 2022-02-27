package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private lateinit var invoiceDal : AntaeusDal
    private lateinit var invoiceService: InvoiceService
    private lateinit var billingService : BillingService

    private val waterproofPaymentProvider = object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
            return true
        }
    }

    @Test
    fun `will do nothing when there is no pending invoice`() = runBlocking {
        // Given
        invoiceDal = mockk {
            every { fetchInvoices() } returns TestDataBuilder.allPaidInvoices
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)
        // When
        billingService.settleAllUnpaid()
        // Then
        TestDataBuilder.allPaidInvoices
            .map { invoice -> invoice.id }
            .forEach { id ->
                verify(exactly = 0) { invoiceService.updateInvoiceStatus(id, ofType()) }
            }
    }

    @Test
    fun `will mark as paid all pending invoices`() = runBlocking {
        // Given
        invoiceDal = mockk {
            every { fetchInvoices() } returns TestDataBuilder.pendingInvoices
            every { updateInvoiceStatus(ofType(), ofType()) } returns 1
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)
        // When

        billingService.settleAllUnpaid()

        // Then
        val ids = TestDataBuilder.pendingInvoices
            .map { invoice -> invoice.id }

        ids
            .forEach { id ->
                verify { invoiceService.updateInvoiceStatus(id, InvoiceStatus.PAID) }
            }
    }
}