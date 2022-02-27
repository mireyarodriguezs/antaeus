package io.pleo.antaeus.core.messaging

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets

class AntaeusMessaging {
    companion object {
        fun <T> antaeusDeliverCallback(block: (strMessage: String) -> T) : DeliverCallback {
            return DeliverCallback { consumerTag: String?, delivery: Delivery ->
                val message = String(delivery.body, StandardCharsets.UTF_8)
                println("[$consumerTag] Received message: '$message'")
                block(message)
            }
        }

        fun <T> antaeusCancelCallback(block: (consumerTag: String?) -> T) : CancelCallback {
            return CancelCallback { consumerTag: String? -> block(consumerTag) }
        }
    }
}