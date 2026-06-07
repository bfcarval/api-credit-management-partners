package api.com.credit.management.partners.infra.messaging.producer

import api.com.credit.management.partners.application.port.output.NotificationMessagePort
import api.com.credit.management.partners.domain.exception.MessagingOperationException
import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitMQNotificationProducer(
    private val rabbitTemplate: RabbitTemplate
) : NotificationMessagePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendTransactionNotification(transactionNotificationEvent: TransactionNotificationEvent) {
        val transactionId = transactionNotificationEvent.transactionId
        val partnerId = transactionNotificationEvent.partnerId

        log.info("Tentando publicar evento de notificacao para a transacao ID: {} do parceiro: {}", transactionId, partnerId)

        try {
            rabbitTemplate.convertAndSend(
                "credit.operations.exchange",
                "credit.transaction.completed",
                transactionNotificationEvent
            ).also {
                log.info("Evento da transacao ID: {} publicado com sucesso no RabbitMQ.", transactionId)
            }

        } catch (ex: Exception) {
            when (ex) {
                is AmqpException -> {
                    log.error("Erro critico de infraestrutura ao tentar conectar ou enviar mensagem ao RabbitMQ para a transacao: $transactionId", ex)
                    throw MessagingOperationException("Falha operacional ao publicar evento de notificacao no broker", ex)
                }
                else -> {
                    log.error("Erro inesperado e desconhecido ao tentar enviar mensagem para a transacao: $transactionId", ex)
                    throw RuntimeException("Erro interno desconhecido no sistema de mensageria", ex)
                }
            }
        }
    }
}
