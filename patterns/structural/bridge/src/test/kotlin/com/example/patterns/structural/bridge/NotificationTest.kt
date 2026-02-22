package com.example.patterns.structural.bridge

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationTest {
    @Test
    fun should_deliverAlertViaEmail_when_emailChannelIsUsed() {
        val notification = AlertNotification(EmailChannel(), severity = "CRITICAL")

        val result = notification.send("ops@example.com")

        assertContains(result, "EMAIL")
        assertContains(result, "ops@example.com")
        assertContains(result, "CRITICAL")
    }

    @Test
    fun should_deliverAlertViaSms_when_smsChannelIsUsed() {
        val notification = AlertNotification(SmsChannel(), severity = "WARNING")

        val result = notification.send("+33612345678")

        assertContains(result, "SMS")
        assertContains(result, "+33612345678")
        assertContains(result, "WARNING")
    }

    @Test
    fun should_deliverPromotionViaSlack_when_slackChannelIsUsed() {
        val notification = PromotionNotification(SlackChannel(), campaignName = "Summer Sale")

        val result = notification.send("#marketing")

        assertContains(result, "SLACK")
        assertContains(result, "#marketing")
        assertContains(result, "Summer Sale")
    }

    @Test
    fun should_combineFreelyAbstractionAndImplementor() {
        val channels = listOf(EmailChannel(), SmsChannel(), SlackChannel())
        val results =
            channels.map { channel ->
                PromotionNotification(channel, campaignName = "Black Friday").send("user-1")
            }

        assertEquals(3, results.size)
        assertTrue(results[0].startsWith("EMAIL"))
        assertTrue(results[1].startsWith("SMS"))
        assertTrue(results[2].startsWith("SLACK"))
    }

    @Test
    fun should_prepareCorrectTitleAndBody_when_alertIsPrepared() {
        val alert = AlertNotification(EmailChannel(), severity = "HIGH")

        val (title, body) = alert.prepare()

        assertEquals("Alert [HIGH]", title)
        assertContains(body, "HIGH")
        assertContains(body, "Immediate action required")
    }
}
