package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.InvoiceDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private lateinit var invoiceDal : InvoiceDal
    private lateinit var invoiceService: InvoiceService
    private lateinit var billingService : BillingService

    private val waterproofPaymentProvider = object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
            return true
        }
    }

    @Test
    fun `will get all pending ids when there are many`() {
        // Given
        invoiceDal = mockk {
            every { fetchInvoices() } returns TestDataBuilder.pendingInvoices
            every { updateInvoiceStatus(ofType(), ofType()) } returns 1
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)

        // When
        var pendingIds = billingService.getByStatus(InvoiceStatus.PENDING)
            .map { invoice -> invoice.id }

        // Then the pending ids we get is composed by all the ids from the pending dataset
        val ids = TestDataBuilder.pendingInvoices
            .map { invoice -> invoice.id }

        assert(pendingIds == ids)
    }

    @Test
    fun `will get zero pending ids when there are none`() {
        // Given
        invoiceDal = mockk {
            every { fetchInvoices() } returns TestDataBuilder.allPaidInvoices
            every { updateInvoiceStatus(ofType(), ofType()) } returns 1
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)

        // When
        val pendingInvoices = billingService.getByStatus(InvoiceStatus.PENDING)

        // Then the pending ids we get is empty list
        val emptyList : List<Invoice>  = emptyList()
        assert(pendingInvoices == emptyList)
    }

    @Test
    fun `will settle invoices for its id`() = runBlocking {
        // Given
        invoiceDal = mockk {
            every { fetchInvoices() } returns TestDataBuilder.pendingInvoices
            every { updateInvoiceStatus(ofType(), ofType()) } returns 1
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)
        val idsToProcess = listOf(TestDataBuilder.pendingInvoices[0].id, TestDataBuilder.pendingInvoices[2].id)

        // When
        billingService.settleForIds(idsToProcess)

        // Then we update the status of all invoice which id its specified and none of the rest
        idsToProcess.forEach { id ->
            verify { invoiceService.updateInvoiceStatus(id, InvoiceStatus.PAID) }
        }
        TestDataBuilder.pendingInvoices
            .filter { i -> !idsToProcess.contains(i.id) }
            .forEach { i ->
                verify(exactly = 0) { invoiceService.updateInvoiceStatus(i.id, InvoiceStatus.PAID) }
            }
    }

    @Test
    fun `will try settle a single invoice and return if the result is paid`() = runBlocking {
        // Given
        val singleInvoice = TestDataBuilder.pendingInvoices[0]

        invoiceDal = mockk {
            every { fetchInvoice(singleInvoice.id) } returns singleInvoice
            every { updateInvoiceStatus(ofType(), ofType()) } returns 1
        }
        invoiceService = InvoiceService(dal = invoiceDal)
        billingService = BillingService(PaymentFacade(waterproofPaymentProvider), invoiceService)

        // When
        billingService.settleForId(singleInvoice.id)

        // Then we verify the call is done and its result is true
        verify (exactly = 1){ invoiceService.updateInvoiceStatus(singleInvoice.id, InvoiceStatus.PAID) }
        assert(invoiceService.updateInvoiceStatus(singleInvoice.id, InvoiceStatus.PAID))
    }
}
