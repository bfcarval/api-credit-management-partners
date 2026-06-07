package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import api.com.credit.management.partners.infra.db.mongo.repository.TransactionMongoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TransactionRepositoryAdapter(
    private val transactionMongoRepository: TransactionMongoRepository
) : TransactionRepositoryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(transaction: TransactionDocument): TransactionDocument =
        try {
            val partnerId = transaction.partnerId
            log.info("Iniciando persistencia de transacao para o parceiro: {}, Tipo: {}", partnerId, transaction.type)

            transactionMongoRepository.save(transaction).also {
                log.info("Transacao salva com sucesso no MongoDB para o parceiro: {}. ID: {}", partnerId, it.id)
            }

        } catch (ex: Exception) {
            log.error("Erro critico de infraestrutura ao salvar transacao para o parceiro: ${transaction.partnerId}", ex)
            throw DatabaseOperationException("Falha operacional ao salvar transacao no banco de dados", ex)
        }

    override fun findByPartnerId(partnerId: String): List<TransactionDocument> =
        try {
            log.info("Iniciando busca de historico de transacoes para o parceiro: {}", partnerId)

            val transactions = transactionMongoRepository.findByPartnerId(partnerId)

            if (transactions.isEmpty()) {
                log.warn("Nenhum historico de transacoes localizado para o parceiro: {}", partnerId)
                throw PartnerAccountNotFoundException("Historico de transacoes nao encontrado para o parceiro: $partnerId")
            }

            transactions.also {
                log.info("Foram localizadas {} transacoes para o parceiro: {}", transactions.size, partnerId)
            }

        } catch (ex: Exception) {
            when (ex) {
                is PartnerAccountNotFoundException -> throw ex
                else -> {
                    log.error("Erro critico de infraestrutura ao buscar transacoes do parceiro: $partnerId", ex)
                    throw DatabaseOperationException("Falha operacional ao buscar historico de transacoes", ex)
                }
            }
        }

    override fun findByStatusAndCreatedAtBefore(status: String, threshold: LocalDateTime): List<TransactionDocument> =
        try {
            log.info("Buscando transacoes com status [{}] criadas antes de: {}", status, threshold)

            transactionMongoRepository.findByStatusAndCreatedAtBefore(status, threshold).also {
                log.info("Encontradas {} transacoes elegiveis para reprocessamento.", it.size)
            }

        } catch (ex: Exception) {
            log.error("Erro inesperado no motor de busca assincrona.", ex)
            throw RuntimeException("Erro interno no scheduler de conciliacao", ex)
        }
}
