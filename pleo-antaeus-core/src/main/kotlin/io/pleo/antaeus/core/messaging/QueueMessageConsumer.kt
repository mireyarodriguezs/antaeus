package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.ConnectionFactory

class QueueMessageConsumer(private val queueName : String, private val consumerTag: String?){
    fun<T> listen(onDeliver: ((strMessage: String) -> T), onCancellation: ((consumerTag: String?) -> T)){
        val connection = ConnectionFactory().newConnection()
        val channel = connection.createChannel()
        channel.basicConsume(
            queueName,
            true,
            consumerTag,
            AntaeusMessaging.antaeusDeliverCallback(onDeliver),
            AntaeusMessaging.antaeusCancelCallback(onCancellation))
    }
}