package api.com.credit.management.partners.infra.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val NOTIFICATION_QUEUE = "credit.notification.queue"
        const val NOTIFICATION_DLQ = "credit.notification.dlq"
        const val CREDIT_EXCHANGE = "credit.operations.exchange"
        const val NOTIFICATION_ROUTING_KEY = "credit.transaction.completed"
    }

    @Bean
    fun messageConverter(): JacksonJsonMessageConverter = JacksonJsonMessageConverter()

    @Bean
    fun notificationQueue(): Queue {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
            .build()
    }

    @Bean
    fun deadLetterQueue(): Queue = QueueBuilder.durable(NOTIFICATION_DLQ).build()

    @Bean
    fun creditExchange(): TopicExchange = ExchangeBuilder.topicExchange(CREDIT_EXCHANGE).durable(true).build()

    @Bean
    fun bindingNotification(notificationQueue: Queue, creditExchange: TopicExchange): Binding {
        return BindingBuilder.bind(notificationQueue).to(creditExchange).with(NOTIFICATION_ROUTING_KEY)
    }
}