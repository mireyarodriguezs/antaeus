package io.pleo.antaeus.data

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.SubscriptionStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CustomerDal (private val db: Database) {
    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
                it[this.subscriptionStatus] = SubscriptionStatus.ACTIVE.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id)
    }

    fun changeSubscriptionStatus(customerId: Int, subscriptionStatus: SubscriptionStatus) {
        return transaction(db) {
            CustomerTable
                .update({ CustomerTable.id.eq(customerId) }) {
                    it[this.subscriptionStatus] = subscriptionStatus.toString()
                }
        }
    }
}