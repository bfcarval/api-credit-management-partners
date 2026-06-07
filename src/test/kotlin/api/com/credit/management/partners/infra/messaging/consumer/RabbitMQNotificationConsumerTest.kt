package api.com.credit.management.partners.infra.messaging.consumer

import api.com.credit.management.partners.domain.exception.ExternalIntegrationException
import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class RabbitMQNotificationConsumerTest {

    private val consumer = RabbitMQNotificationConsumer()

    @Test
    fun `deve consumir evento de notificacao com sucesso`() {
        val event = TransactionNotificationEvent(
            transactionId = "tx-123",
            partnerId = "partner-valido",
            amount = BigDecimal("100.00"),
            type = "CREDIT"
        )

        assertDoesNotThrow {
            consumer.consumeTransactionNotification(event)
        }
    }

    @Test
    fun `deve lancar ExternalIntegrationException quando o parceiro simular falha de integracao`() {
        val event = TransactionNotificationEvent(
            transactionId = "tx-456",
            partnerId = "teste-falha-integracao",
            amount = BigDecimal("50.00"),
            type = "DEBIT"
        )

        assertThrows<ExternalIntegrationException> {
            consumer.consumeTransactionNotification(event)
        }
    }
}
