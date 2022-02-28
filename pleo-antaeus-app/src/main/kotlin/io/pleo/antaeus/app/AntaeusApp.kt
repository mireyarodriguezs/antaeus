/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import io.pleo.antaeus.core.external.PaymentFacade
import io.pleo.antaeus.core.external.RandomPaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.CustomerDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceDal
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.messageconsumer.MessageConsumer
import io.pleo.antaeus.rest.AntaeusRest
import io.pleo.antaeus.scheduler.BillingScheduler
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".sqlite")
    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect(url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC",
            user = "root",
            password = "")
        .also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val invoiceDal = InvoiceDal(db = db)
    val customerDal = CustomerDal(db = db)

    // Insert example data in the database.
    DataSetup.setupInitialData(customerDal = customerDal, invoiceDal = invoiceDal)

    // Get third parties
    val paymentProvider = RandomPaymentProvider.getPaymentProvider()

    // Create core services
    val invoiceService = InvoiceService(dal = invoiceDal)
    val customerService = CustomerService(dal = customerDal)

    val paymentFacade = PaymentFacade(paymentProvider = paymentProvider)

    val billingService = BillingService(paymentFacade, invoiceService)

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService
    ).run()

    // Run scheduler
    BillingScheduler(billingService = billingService).start()
    // Run consumer
    MessageConsumer(billingService = billingService).listen()
}
