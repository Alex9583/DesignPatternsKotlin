package com.example.patterns.behavioral.templatemethod

data class NotificationResult(
    val channel: String,
    val recipient: String,
    val success: Boolean,
    val formattedMessage: String,
)

abstract class NotificationSender {
    fun send(
        recipient: String,
        subject: String,
        body: String,
    ): NotificationResult {
        validate(recipient)
        val formattedMessage = format(subject, body)
        val success = deliver(recipient, formattedMessage)
        val result =
            NotificationResult(
                channel = channelName(),
                recipient = recipient,
                success = success,
                formattedMessage = formattedMessage,
            )
        log(result)
        return result
    }

    protected abstract fun channelName(): String

    protected abstract fun validate(recipient: String)

    protected abstract fun format(
        subject: String,
        body: String,
    ): String

    protected abstract fun deliver(
        recipient: String,
        message: String,
    ): Boolean

    protected open fun log(result: NotificationResult) {
        // Default no-op — subclasses may override to add logging behavior
    }
}

class EmailNotificationSender : NotificationSender() {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    override fun channelName(): String = "email"

    override fun validate(recipient: String) {
        require(emailRegex.matches(recipient)) {
            "Invalid email address: '$recipient'"
        }
    }

    override fun format(
        subject: String,
        body: String,
    ): String = "<html><head><title>$subject</title></head><body><h1>$subject</h1><p>$body</p></body></html>"

    override fun deliver(
        recipient: String,
        message: String,
    ): Boolean = true
}

class SmsNotificationSender : NotificationSender() {
    companion object {
        const val MAX_SMS_LENGTH = 160
    }

    override fun channelName(): String = "sms"

    override fun validate(recipient: String) {
        require(recipient.startsWith("+")) {
            "Invalid phone number: '$recipient' (must start with '+')"
        }
    }

    override fun format(
        subject: String,
        body: String,
    ): String {
        val full = "$subject: $body"
        return if (full.length > MAX_SMS_LENGTH) {
            full.take(MAX_SMS_LENGTH - 3) + "..."
        } else {
            full
        }
    }

    override fun deliver(
        recipient: String,
        message: String,
    ): Boolean = true
}
