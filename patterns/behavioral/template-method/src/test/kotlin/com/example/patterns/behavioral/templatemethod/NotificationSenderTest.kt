package com.example.patterns.behavioral.templatemethod

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NotificationSenderTest {
    @Test
    fun should_sendEmail_when_recipientIsValid() {
        val sender: NotificationSender = EmailNotificationSender()

        val result =
            sender.send(
                recipient = "user@example.com",
                subject = "Order Confirmation",
                body = "Your order #1234 has been confirmed.",
            )

        assertTrue(result.success)
        assertEquals("email", result.channel)
        assertEquals("user@example.com", result.recipient)
        assertTrue(result.formattedMessage.contains("<h1>Order Confirmation</h1>"))
        assertTrue(result.formattedMessage.contains("<p>Your order #1234 has been confirmed.</p>"))
    }

    @Test
    fun should_truncateSmsMessage_when_bodyExceeds160Characters() {
        val sender: NotificationSender = SmsNotificationSender()
        val longBody = "A".repeat(200)

        val result =
            sender.send(
                recipient = "+33612345678",
                subject = "Alert",
                body = longBody,
            )

        assertTrue(result.success)
        assertEquals("sms", result.channel)
        assertEquals(SmsNotificationSender.MAX_SMS_LENGTH, result.formattedMessage.length)
        assertTrue(result.formattedMessage.endsWith("..."))
    }

    @Test
    fun should_keepSmsMessage_when_bodyFitsWithin160Characters() {
        val sender: NotificationSender = SmsNotificationSender()

        val result =
            sender.send(
                recipient = "+33612345678",
                subject = "Alert",
                body = "Short message",
            )

        assertTrue(result.success)
        assertEquals("Alert: Short message", result.formattedMessage)
    }

    @Test
    fun should_throwException_when_emailFormatIsInvalid() {
        val sender: NotificationSender = EmailNotificationSender()

        val exception =
            assertFailsWith<IllegalArgumentException> {
                sender.send(
                    recipient = "not-an-email",
                    subject = "Test",
                    body = "Body",
                )
            }

        assertTrue(exception.message!!.contains("Invalid email address"))
    }

    @Test
    fun should_throwException_when_phoneNumberMissesPlusPrefix() {
        val sender: NotificationSender = SmsNotificationSender()

        val exception =
            assertFailsWith<IllegalArgumentException> {
                sender.send(
                    recipient = "0612345678",
                    subject = "Test",
                    body = "Body",
                )
            }

        assertTrue(exception.message!!.contains("must start with '+'"))
    }

    @Test
    fun should_enforceTemplateMethodStepOrder_when_subclassOverridesSteps() {
        val callOrder = mutableListOf<String>()

        val spySender =
            object : NotificationSender() {
                override fun channelName(): String = "spy"

                override fun validate(recipient: String) {
                    callOrder.add("validate")
                }

                override fun format(
                    subject: String,
                    body: String,
                ): String {
                    callOrder.add("format")
                    return "$subject - $body"
                }

                override fun deliver(
                    recipient: String,
                    message: String,
                ): Boolean {
                    callOrder.add("deliver")
                    return true
                }

                override fun log(result: NotificationResult) {
                    callOrder.add("log")
                }
            }

        spySender.send("recipient", "Subject", "Body")

        assertEquals(listOf("validate", "format", "deliver", "log"), callOrder)
    }
}
