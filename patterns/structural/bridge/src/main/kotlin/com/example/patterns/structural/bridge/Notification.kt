package com.example.patterns.structural.bridge

interface Channel {
    fun deliver(
        recipient: String,
        title: String,
        body: String,
    ): String
}

class EmailChannel : Channel {
    override fun deliver(
        recipient: String,
        title: String,
        body: String,
    ): String = "EMAIL to=$recipient subject=\"$title\" body=\"$body\""
}

class SmsChannel : Channel {
    override fun deliver(
        recipient: String,
        title: String,
        body: String,
    ): String = "SMS to=$recipient message=\"$title: $body\""
}

class SlackChannel : Channel {
    override fun deliver(
        recipient: String,
        title: String,
        body: String,
    ): String = "SLACK channel=$recipient header=\"$title\" text=\"$body\""
}

abstract class Notification(
    protected val channel: Channel,
) {
    abstract fun prepare(): Pair<String, String>

    fun send(recipient: String): String {
        val (title, body) = prepare()
        return channel.deliver(recipient, title, body)
    }
}

class AlertNotification(
    channel: Channel,
    private val severity: String,
) : Notification(channel) {
    override fun prepare(): Pair<String, String> =
        "Alert [$severity]" to "An incident of severity $severity has been detected. Immediate action required."
}

class PromotionNotification(
    channel: Channel,
    private val campaignName: String,
) : Notification(channel) {
    override fun prepare(): Pair<String, String> =
        "Promotion: $campaignName" to "Don't miss our $campaignName campaign! Limited-time offers inside."
}
