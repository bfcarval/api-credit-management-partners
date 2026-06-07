package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.NotificationMessagePort
import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReconcilePendingTransactionsUseCase(
    private val transactionRepositoryPort: TransactionRepositoryPort,
    private val notificationPort: NotificationMessagePort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute() {
        val thresholdTime = LocalDateTime.now().minusMinutes(5)
        log.info("Buscando transacoes travadas em PENDING criadas antes de: {}", thresholdTime)

        try {
            val pendingTransactions = transactionRepositoryPort.findByStatusAndCreatedAtBefore(
                status = TransactionStatusType.PENDING.name,
                threshold = thresholdTime
            )

            if (pendingTransactions.isEmpty()) {
                log.info("Nenhuma transacao pendente localizada para processamento.")
                return
            }

            log.info("Encontradas {} transacoes elegiveis para reprocessamento.", pendingTransactions.size)

            for (transaction in pendingTransactions) {
                val transactionId = transaction.id
                val partnerId = transaction.partnerId

                try {
                    log.info("Tentando reprocessar a transacao ID: {} para o parceiro: {}", transactionId, partnerId)

                    notificationPort.sendTransactionNotification(
                        TransactionNotificationEvent(
                            transactionId = transactionId!!,
                            partnerId = partnerId,
                            amount = transaction.amount,
                            type = transaction.type.name
                        )
                    )

                    transactionRepositoryPort.save(
                        transaction.copy(
                            status = TransactionStatusType.COMPLETED,
                            completedAt = LocalDateTime.now()
                        )
                    ).also {
                        log.info("Transacao ID: {} resolvida com sucesso.", transactionId)
                    }

                } catch (e: Exception) {
                    log.error("Falha critica ao tentar processar a transacao ID: $transactionId. Ela permanecera em PENDING.", e)
                }
            }

        } catch (ex: DatabaseOperationException) {
            log.error("Interrupcao forcada devido a falha geral no banco de dados.", ex)
            throw ex
        } catch (ex: Exception) {
            log.error("Ocorreu um erro inesperado no ciclo de varredura.", ex)
            throw ex
        }
    }
}
