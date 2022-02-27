package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentFacade: PaymentFacade,
    private val invoiceService: InvoiceService) {

    suspend fun settleAllUnpaid() {
        invoiceService.fetchAll()
            .filter { invoice -> invoice.status == InvoiceStatus.PENDING }
            .forEach { invoice ->
                if (paymentFacade.charge(invoice))
                    invoiceService.updateInvoiceStatus(invoice.id, InvoiceStatus.PAID) }
    }

    suspend fun settleForIds(idList: List<Int>) {
        invoiceService.fetchAll()
            .filter { invoice -> idList.contains(invoice.id)}
            .filter { invoice -> invoice.status == InvoiceStatus.PENDING }
            .forEach { invoice ->
                if (paymentFacade.charge(invoice))
                    invoiceService.updateInvoiceStatus(invoice.id, InvoiceStatus.PAID) }
    }

    fun getAllPendingIds() : List<Int> {
        return  invoiceService.fetchAll()
            .filter { invoice -> invoice.status == InvoiceStatus.PENDING }
            .map { invoice -> invoice.id }
    }
}
