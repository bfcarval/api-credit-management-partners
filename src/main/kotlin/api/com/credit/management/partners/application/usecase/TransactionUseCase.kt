package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionUseCase(
    private val transactionRepositoryPort: TransactionRepositoryPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun getTransactionHistory(partnerId: String): List<TransactionDocument> {
        log.info("Iniciando consulta de historico de transacoes para o parceiro: {}", partnerId)

        try {
            val history = transactionRepositoryPort.findByPartnerId(partnerId)

            log.info("Foram localizadas {} transacoes para o parceiro: {}", history.size, partnerId)

            return history.sortedByDescending { it.createdAt }

        } catch (ex: Exception) {
            when (ex) {
                is PartnerAccountNotFoundException -> {
                    log.warn("Historico de transacoes nao localizado para o parceiro: {}", partnerId)
                    throw ex
                }
                is DatabaseOperationException -> {
                    log.error("Erro operacional de banco de dados ao buscar extrato do parceiro: $partnerId", ex)
                    throw ex
                }
                else -> {
                    log.error("Erro inesperado e desconhecido ao processar historico do parceiro: $partnerId", ex)
                    throw RuntimeException("Erro interno ao processar a consulta de extrato", ex)
                }
            }
        }
    }
}
