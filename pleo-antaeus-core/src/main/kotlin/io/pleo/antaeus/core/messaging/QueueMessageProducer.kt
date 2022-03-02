package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class QueueMessageProducer(){
    inline fun <reified T> sendMessage(message: T, exchangeName: String, queueName: String, routingKey: String = "default") {
        val factory = ConnectionFactory()
        factory.newConnection(LocalQueue.getUri()).use { connection ->
            var channel = connection.createChannel()
            channel.exchangeDeclare(exchangeName,"direct", true)
            channel.queueDeclare(queueName, true, false, false, emptyMap())
            channel.queueBind(queueName, exchangeName, routingKey)
            val messageJson = Json.encodeToString(message)
            channel.basicPublish(
                exchangeName,
                routingKey,
                null,
                messageJson.toByteArray(StandardCharsets.UTF_8)
            )
            channel.close()
            println(" [x] Sent '$message'")
        }
    }
}