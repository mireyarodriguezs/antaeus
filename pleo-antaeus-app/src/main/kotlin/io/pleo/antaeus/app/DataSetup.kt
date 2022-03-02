package io.pleo.antaeus.app

import io.pleo.antaeus.data.CustomerDal
import io.pleo.antaeus.data.InvoiceDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.joda.time.DateTime
import java.math.BigDecimal
import kotlin.random.Random

class DataSetup {
    companion object{
        // This will create all schemas and setup initial data
        internal fun setupInitialData(customerDal: CustomerDal, invoiceDal: InvoiceDal) {
            val customers = (1..100).mapNotNull {
                customerDal.createCustomer(
                    currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
                )
            }

            customers.forEach { customer ->
                (1..10).forEach {
                    invoiceDal.createInvoice(
                        amount = Money(
                            value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                            currency = customer.currency
                        ),
                        customer = customer,
                        status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID,
                        dueDate = DateTime.parse("2022-03-01")
                    )
                }
            }
    }
}

}