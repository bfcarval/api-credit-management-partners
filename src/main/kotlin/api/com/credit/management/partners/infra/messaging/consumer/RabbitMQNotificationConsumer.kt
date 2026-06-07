package api.com.credit.management.partners.infra.messaging.consumer

import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import api.com.credit.management.partners.domain.exception.ExternalIntegrationException // 🌟 Crie esta classe no seu pacote de exceptions
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class RabbitMQNotificationConsumer {

    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = ["credit.notification.queue"])
    fun consumeTransactionNotification(event: TransactionNotificationEvent) {
        log.info(
            "Recebendo notificacao - ID Transacao: {}, Parceiro: {}, Tipo: {}, Valor: {}",
            event.transactionId,
            event.partnerId,
            event.type,
            event.amount
        )

        try {
            processExternalNotification(event)

            log.info("Notificacao da transacao {} integrada com sucesso.", event.transactionId)
        } catch (ex: Exception) {
            when (ex) {
                is ExternalIntegrationException -> {
                    log.error("Falha de integracao externa na transacao: ${event.transactionId}. Acionando mecanismo de retry/DLQ.", ex)
                    throw ex
                }
                else -> {
                    log.error(" Erro inesperado no processamento da transacao: ${event.transactionId}", ex)
                    throw ex
                }
            }
        }
    }

    private fun processExternalNotification(event: TransactionNotificationEvent) {
        Thread.sleep(100)

        if (event.partnerId == "teste-falha-integracao") {
            throw ExternalIntegrationException("Servidor externo do parceiro B2B respondeu com HTTP 503 Service Unavailable")
        }
    }
}
