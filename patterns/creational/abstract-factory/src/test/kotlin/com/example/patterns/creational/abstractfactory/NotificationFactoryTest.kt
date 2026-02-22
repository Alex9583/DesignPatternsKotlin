package com.example.patterns.creational.abstractfactory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationFactoryTest {
    private val content =
        NotificationContent(
            recipient = "user@example.com",
            subject = "Order Shipped",
            body = "Your order #1234 has been shipped.",
        )

    @Test
    fun should_sendEmailNotification_when_emailFactoryUsed() {
        val service = NotificationService(EmailNotificationFactory())
        val result = service.notify(content)

        assertTrue(result.delivered)
        assertEquals("email", result.channel)
    }

    @Test
    fun should_sendSmsNotification_when_smsFactoryUsed() {
        val service = NotificationService(SmsNotificationFactory())
        val result = service.notify(content)

        assertTrue(result.delivered)
        assertEquals("sms", result.channel)
    }

    @Test
    fun should_sendPushNotification_when_pushFactoryUsed() {
        val service = NotificationService(PushNotificationFactory())
        val result = service.notify(content)

        assertTrue(result.delivered)
        assertEquals("push", result.channel)
    }

    @Test
    fun should_truncateSmsBody_when_messageExceeds160Chars() {
        val longContent = content.copy(body = "A".repeat(200))
        val formatter = SmsFormatter()
        val formatted = formatter.format(longContent)

        assertTrue(formatted.length <= 160)
        assertTrue(formatted.endsWith("..."))
    }

    @Test
    fun should_formatEmailWithHeaders_when_emailFormatterUsed() {
        val formatter = EmailFormatter()
        val formatted = formatter.format(content)

        assertTrue(formatted.contains("Subject: Order Shipped"))
        assertTrue(formatted.contains("To: user@example.com"))
        assertTrue(formatted.contains("Your order #1234 has been shipped."))
    }

    @Test
    fun should_swapFactoryWithoutChangingClient_when_differentChannelNeeded() {
        val factories: List<NotificationFactory> =
            listOf(
                EmailNotificationFactory(),
                SmsNotificationFactory(),
                PushNotificationFactory(),
            )

        val results =
            factories.map { factory ->
                NotificationService(factory).notify(content)
            }

        assertEquals(listOf("email", "sms", "push"), results.map { it.channel })
        assertTrue(results.all { it.delivered })
    }
}
