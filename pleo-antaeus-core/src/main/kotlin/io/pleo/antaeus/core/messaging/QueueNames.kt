package io.pleo.antaeus.core.messaging

class QueueNames {
    companion object {
        val pendingInvoicesQueueName : String = "PendingInvoicesQueue"
        val DueInvoicesQueue : String = "DueInvoicesQueue"
        val PendingInvoicesConsumerTag : String = "pending-invoices"
        val PendingInvoicesRouting : String = "pending-invoices"
        val DueInvoicesConsumerTag : String = "due-invoices"
        val DueInvoicesRouting : String = "due-invoices"
        val InvoiceExchange : String = "InvoiceExchange"
    }
}