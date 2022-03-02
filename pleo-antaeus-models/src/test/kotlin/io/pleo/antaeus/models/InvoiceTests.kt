package io.pleo.antaeus.models

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.sql.Date
import java.util.*
import java.util.stream.Stream

class InvoiceTests {

    @ParameterizedTest(name = "given \"{0}\", when calling is due it should return {1}")
    @MethodSource("statusesAndExpectedResponses")
    fun `given invoice, when calling is due, then is should return expected response`(
        status: InvoiceStatus,
        expected: Boolean
    ) {
        // Given
        val invoice = Invoice(
            1,
            1,
            Money(BigDecimal.valueOf(1000), Currency.EUR),
            status,
            Date.valueOf("2022-03-01"))

        // When
        val actual =  with(InvoiceExtensions()) {
            invoice.isDue()
        }

        // Then
        assert(actual == expected)
    }
    @ParameterizedTest(name = "given \"{0}\", when calling is isPastGracePeriod it should return {1}")
    @MethodSource("datesAndExpectedResponses")
    fun `given due invoice, when calling is isPastGracePeriod, then is should return expected response`
                (calendar: Calendar, expected: Boolean){
        // Given
        val invoice = Invoice(
            1,
            1,
            Money(BigDecimal.valueOf(1000), Currency.EUR),
            InvoiceStatus.NOTSUFFICIENTFUNDS,
            calendar.time)

        // When
        val actual =  with(InvoiceExtensions()) {
            invoice.isPastGracePeriod()
        }

        // Then
        assert(actual == expected)
    }

    @ParameterizedTest(name = "given \"{0}\", when calling is isPastGracePeriod it should return false")
    @MethodSource("datesAndExpectedResponses")
    fun `given paid invoice, when calling is isPastGracePeriod, then is should return false as its already paid`
                (calendar: Calendar, expected: Boolean){
        // Given
        val invoice = Invoice(
            1,
            1,
            Money(BigDecimal.valueOf(1000), Currency.EUR),
            InvoiceStatus.PAID,
            calendar.time)

        // When
        val actual =  with(InvoiceExtensions()) {
            invoice.isPastGracePeriod()
        }
        // Then
        assert(actual == false)
    }

    private companion object {
        @JvmStatic
        fun statusesAndExpectedResponses(): Stream<Arguments> = Stream.of(
            Arguments.of(InvoiceStatus.PAID, false),
            Arguments.of(InvoiceStatus.INVOICENOTFOUND, false),
            Arguments.of(InvoiceStatus.CUSTOMERNOTFOUND, false),
            Arguments.of(InvoiceStatus.CURRENCYMISMATCH, false),
            Arguments.of(InvoiceStatus.PENDING, true),
            Arguments.of(InvoiceStatus.NOTSUFFICIENTFUNDS, true),
        )

        fun goBackInTime(numberOfDays : Int) : Calendar {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, numberOfDays)
            return calendar
        }

        @JvmStatic
        fun datesAndExpectedResponses(): Stream<Arguments> = Stream.of(
            Arguments.of(goBackInTime(-90), true),
            Arguments.of(goBackInTime(-100), true),
            Arguments.of(goBackInTime(-1), false),
            Arguments.of(Calendar.getInstance(), false),
            Arguments.of(goBackInTime(-31), true),
            Arguments.of(goBackInTime(-30), false),
            Arguments.of(goBackInTime(10), false),
        )
    }
}