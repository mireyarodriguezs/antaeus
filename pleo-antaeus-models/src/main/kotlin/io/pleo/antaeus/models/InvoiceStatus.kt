package io.pleo.antaeus.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    INVOICENOTFOUND,
    CUSTOMERNOTFOUND,
    CURRENCYMISMATCH,
    NOTSUFFICIENTFUNDS
}
