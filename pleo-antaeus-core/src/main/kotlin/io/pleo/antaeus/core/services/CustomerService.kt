/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.core.services

import com.sun.org.apache.xpath.internal.operations.Bool
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.CustomerDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.SubscriptionStatus

class CustomerService(private val dal: CustomerDal) {
    fun fetchAll(): List<Customer> {
        return dal.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return dal.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }

    fun hasActiveSubscription(id: Int) : Boolean =
        dal.fetchCustomer(id)?.subscriptionStatus == SubscriptionStatus.ACTIVE

    fun changeSubscriptionStatus(id: Int, subscriptionStatus: SubscriptionStatus) =
        dal.changeSubscriptionStatus(id, subscriptionStatus)
}
