package com.example.patterns.creational.abstractfactory

data class NotificationContent(
    val recipient: String,
    val subject: String,
    val body: String,
)

data class DeliveryResult(
    val delivered: Boolean,
    val channel: String,
    val details: String,
)

interface MessageFormatter {
    fun format(content: NotificationContent): String
}

interface NotificationSender {
    fun send(
        formattedMessage: String,
        recipient: String,
    ): DeliveryResult
}

interface NotificationFactory {
    fun createFormatter(): MessageFormatter

    fun createSender(): NotificationSender
}

class EmailFormatter : MessageFormatter {
    override fun format(content: NotificationContent): String =
        """
        |Subject: ${content.subject}
        |To: ${content.recipient}
        |
        |${content.body}
        """.trimMargin()
}

class EmailSender : NotificationSender {
    override fun send(
        formattedMessage: String,
        recipient: String,
    ): DeliveryResult = DeliveryResult(delivered = true, channel = "email", details = "Sent to $recipient via SMTP")
}

class EmailNotificationFactory : NotificationFactory {
    override fun createFormatter(): MessageFormatter = EmailFormatter()

    override fun createSender(): NotificationSender = EmailSender()
}

class SmsFormatter : MessageFormatter {
    override fun format(content: NotificationContent): String {
        val truncated = if (content.body.length > 160) content.body.take(157) + "..." else content.body
        return truncated
    }
}

class SmsSender : NotificationSender {
    override fun send(
        formattedMessage: String,
        recipient: String,
    ): DeliveryResult = DeliveryResult(delivered = true, channel = "sms", details = "Sent to $recipient via SMS gateway")
}

class SmsNotificationFactory : NotificationFactory {
    override fun createFormatter(): MessageFormatter = SmsFormatter()

    override fun createSender(): NotificationSender = SmsSender()
}

class PushFormatter : MessageFormatter {
    override fun format(content: NotificationContent): String = """{"title":"${content.subject}","body":"${content.body}"}"""
}

class PushSender : NotificationSender {
    override fun send(
        formattedMessage: String,
        recipient: String,
    ): DeliveryResult = DeliveryResult(delivered = true, channel = "push", details = "Push sent to device $recipient")
}

class PushNotificationFactory : NotificationFactory {
    override fun createFormatter(): MessageFormatter = PushFormatter()

    override fun createSender(): NotificationSender = PushSender()
}

class NotificationService(
    private val factory: NotificationFactory,
) {
    fun notify(content: NotificationContent): DeliveryResult {
        val formatter = factory.createFormatter()
        val sender = factory.createSender()
        val message = formatter.format(content)
        return sender.send(message, content.recipient)
    }
}
