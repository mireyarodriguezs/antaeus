package io.pleo.antaeus.models

import java.util.*

const val GracePeriodInDays = 30

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus,
    val dueDate: Date
)

fun Invoice.isPastGracePeriod() : Boolean {
    val daysInArrears = (Calendar.getInstance().timeInMillis -  this.dueDate.time) / (1000*60*60*24)
    return this.isDue() && daysInArrears > GracePeriodInDays
}

/**
 * Whether the status of the invoice indicates that needs to be processed for payment
 */
fun Invoice.isDue() : Boolean =
    this.status == InvoiceStatus.PENDING || this.status == InvoiceStatus.NOTSUFFICIENTFUNDS