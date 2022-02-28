package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentFacade: PaymentFacade,
    private val invoiceService: InvoiceService) {

    suspend fun settleAllUnpaid() =
        invoiceService.fetchAll()
            .filter { invoice -> invoice.status == InvoiceStatus.PAID }
            .forEach { invoice ->
                val newStatus = paymentFacade.charge(invoice)
                    invoiceService.updateInvoiceStatus(invoice.id, newStatus) }

    suspend fun settleForIds(idList: List<Int>) =
        invoiceService.fetchAll()
            .filter { invoice -> idList.contains(invoice.id)}
            .filter { invoice -> invoice.status != InvoiceStatus.PAID }
            .forEach { invoice ->
                val newStatus = paymentFacade.charge(invoice)
                invoiceService.updateInvoiceStatus(invoice.id, newStatus) }

    fun getByStatus(status : InvoiceStatus) : List<Int>  =
        invoiceService.fetchAll()
            .filter { invoice -> invoice.status == status }
            .map { invoice -> invoice.id }
}
