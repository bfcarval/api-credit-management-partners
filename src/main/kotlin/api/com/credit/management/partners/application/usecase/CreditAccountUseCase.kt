package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.application.port.output.NotificationMessagePort
import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.InsufficientBalanceException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.mapper.toVO
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.domain.model.event.TransactionNotificationEvent
import api.com.credit.management.partners.domain.model.vo.TransactionVO
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class CreditAccountUseCase(
    private val accountRepository: CreditAccountRepositoryPort,
    private val transactionRepositoryPort: TransactionRepositoryPort,
    private val notificationPort: NotificationMessagePort,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Retryable(
        value = [OptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 150,
        multiplier = 2.0
    )
    @CacheEvict(value = ["getBalance"], key = "#partnerId")
    fun addCredits(partnerId: String, amount: BigDecimal, description: String): TransactionVO {
        log.info("Iniciando adicao de creditos para o parceiro: {}, Valor: {}", partnerId, amount)

        val pendingTransaction = transactionRepositoryPort.save(
            TransactionDocument(
                partnerId = partnerId,
                type = TransactionType.CREDIT,
                amount = amount,
                description = description,
                status = TransactionStatusType.PENDING
            )
        )

        try {
            val account = accountRepository.findByPartnerId(partnerId)

            account.addCredits(amount)
            accountRepository.save(account)

            val completedTransaction = transactionRepositoryPort.save(
                pendingTransaction.copy(status = TransactionStatusType.COMPLETED, completedAt = LocalDateTime.now())
            )

            notificationPort.sendTransactionNotification(
                TransactionNotificationEvent(
                    transactionId = completedTransaction.id!!,
                    partnerId = partnerId,
                    amount = completedTransaction.amount,
                    type = TransactionType.CREDIT.name
                )
            )

            return completedTransaction.toVO().also {
                log.info("Creditos adicionados com sucesso para o parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {
            if (ex !is OptimisticLockingFailureException) {
                log.warn("Falha ao adicionar creditos para o parceiro: {}. Marcando transacao como FAILED.", partnerId)
                transactionRepositoryPort.save(pendingTransaction.copy(status = TransactionStatusType.FAILED))
            }

            when (ex) {
                is PartnerAccountNotFoundException, is DatabaseOperationException -> throw ex
                is OptimisticLockingFailureException -> throw ex
                else -> {
                    log.error("Erro inesperado ao adicionar creditos para o parceiro: $partnerId", ex)
                    throw ex
                }
            }
        }
    }

    @Retryable(
        value = [OptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 150,
        multiplier = 2.0
    )
    @CacheEvict(value = ["getBalance"], key = "#partnerId")
    fun debitCredits(partnerId: String, amount: BigDecimal, description: String): TransactionVO {
        log.info("Iniciando consumo de creditos para o parceiro: {}, Valor: {}", partnerId, amount)

        val pendingTransaction = transactionRepositoryPort.save(
            TransactionDocument(
                partnerId = partnerId,
                type = TransactionType.DEBIT,
                amount = amount,
                description = description,
                status = TransactionStatusType.PENDING
            )
        )

        try {
            val account = accountRepository.findByPartnerId(partnerId)

            account.debitCredits(amount)
            accountRepository.save(account)

            val completedTransaction = transactionRepositoryPort.save(
                pendingTransaction.copy(status = TransactionStatusType.COMPLETED, completedAt = LocalDateTime.now())
            )

            notificationPort.sendTransactionNotification(
                TransactionNotificationEvent(completedTransaction.id!!, partnerId, amount, "DEBIT")
            )

            return completedTransaction.toVO().also {
                log.info("Debito processado com sucesso para o parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {
            if (ex !is OptimisticLockingFailureException) {
                log.warn("Falha ao debitar creditos para o parceiro: {}. Marcando transacao como FAILED.", partnerId)
                transactionRepositoryPort.save(pendingTransaction.copy(status = TransactionStatusType.FAILED))
            }

            when (ex) {
                is PartnerAccountNotFoundException, is DatabaseOperationException, is OptimisticLockingFailureException -> throw ex
                is InsufficientBalanceException -> {
                    log.warn("Saldo insuficiente detectado para o parceiro: {}", partnerId)
                    throw ex
                }
                else -> {
                    log.error("Erro inesperado ao debitar creditos para o parceiro: $partnerId", ex)
                    throw ex
                }
            }
        }
    }
}
