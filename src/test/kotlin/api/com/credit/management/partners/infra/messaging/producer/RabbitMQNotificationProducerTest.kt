package api.com.credit.management.partners.infra.messaging.producer

import api.com.credit.management.partners.domain.exception.MessagingOperationException
import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class RabbitMQNotificationProducerTest {

    private val rabbitTemplate = mockk<RabbitTemplate>(relaxed = true)
    private val producer = RabbitMQNotificationProducer(rabbitTemplate)

    private val event = TransactionNotificationEvent(
        transactionId = "tx-123",
        partnerId = "partner-99",
        amount = BigDecimal("150.00"),
        type = "CREDIT"
    )

    @Test
    fun `deve publicar evento de notificacao com sucesso no rabbitmq`() {
        every { 
            rabbitTemplate.convertAndSend(
                "credit.operations.exchange", 
                "credit.transaction.completed", 
                event
            ) 
        } returns Unit

        producer.sendTransactionNotification(event)

        verify(exactly = 1) { 
            rabbitTemplate.convertAndSend(
                "credit.operations.exchange", 
                "credit.transaction.completed", 
                event
            ) 
        }
    }

    @Test
    fun `deve repassar a excecao original quando ocorrer um erro inesperado nao mapeado`() {
        every { 
            rabbitTemplate.convertAndSend(
                "credit.operations.exchange", 
                "credit.transaction.completed", 
                event
            ) 
        } throws RuntimeException("Erro catastrophico")

        assertThrows<RuntimeException> {
            producer.sendTransactionNotification(event)
        }
    }
}
