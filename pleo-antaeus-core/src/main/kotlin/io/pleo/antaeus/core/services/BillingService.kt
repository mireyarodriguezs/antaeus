package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.isDue

class BillingService(
    private val paymentFacade: PaymentFacade,
    private val invoiceService: InvoiceService) {

    /**
     * Will try to make payment effective for the given invoiceId
     * Returns whether the invoice is considered paid or not
     */
    suspend fun settleForId(invoiceId: Int) : Boolean {
        var invoice = invoiceService.fetch(invoiceId)
        if (invoice.isDue()) {
            val newStatus = paymentFacade.charge(invoice)
            invoiceService.updateInvoiceStatus(invoice.id, newStatus)
            return newStatus == InvoiceStatus.PAID
        }

        return false
    }

    /**
     * Will try to make payment effective for a list of invoiceIds
     */
    suspend fun settleForIds(idList: List<Int>) =
        invoiceService.fetchAll()
            .filter { invoice -> idList.contains(invoice.id) }
            .filter { invoice -> invoice.isDue() }
            .forEach { invoice ->
                val newStatus = paymentFacade.charge(invoice)
                invoiceService.updateInvoiceStatus(invoice.id, newStatus)
            }
    /**
     * Returns all the invoices matching a given status
     */
    fun getByStatus(status : InvoiceStatus) : List<Invoice>  =
        invoiceService.fetchAll()
            .filter { invoice -> invoice.status == status }
}
