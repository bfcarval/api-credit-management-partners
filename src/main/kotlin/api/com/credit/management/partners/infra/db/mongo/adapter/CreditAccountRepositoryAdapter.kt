package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.mapper.toDocument
import api.com.credit.management.partners.domain.mapper.toModel
import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.infra.db.mongo.repository.CreditAccountMongoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class CreditAccountRepositoryAdapter(
    private val repo: CreditAccountMongoRepository
) : CreditAccountRepositoryPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun findByPartnerId(partnerId: String): CreditAccountModel =
        try {
            log.info("Iniciando busca de conta de credito para o parceiro: {}", partnerId)

            val documentOptional = repo.findByPartnerId(partnerId)

            if (documentOptional.isEmpty) {
                log.warn("Nenhuma conta de credito encontrada para o parceiro: {}", partnerId)
                throw PartnerAccountNotFoundException("Conta de credito nao encontrada para o parceiro: $partnerId")
            }

            documentOptional.get().toModel().also {
                log.info("Conta de credito localizada com sucesso para o parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {

            when (ex) {
                is PartnerAccountNotFoundException -> throw ex
                else -> {
                    log.error("Erro critico de infraestrutura ao acessar o MongoDB para o parceiro: $partnerId", ex)
                    throw DatabaseOperationException("Erro interno desconhecido ao processar a conta", ex)
                }
            }
        }

    override fun findNewPartnerId(partnerId: String): Optional<CreditAccountModel> =
        try {
            log.info("Iniciando busca de conta de credito para o parceiro: {}", partnerId)

            val optionalAccount = repo.findByPartnerId(partnerId)

            if (optionalAccount.isPresent) {
                Optional.of<CreditAccountModel>(optionalAccount.get().toModel())
            } else {
                Optional.empty<CreditAccountModel>()
            }

        } catch (ex: Exception) {
            log.error("Erro critico de infraestrutura ao acessar o MongoDB para o parceiro: $partnerId", ex)
            throw DatabaseOperationException("Erro interno desconhecido ao processar a conta", ex)
        }

    override fun save(creditAccountModel: CreditAccountModel): CreditAccountModel =
        try {
            val partnerId = creditAccountModel.partnerId
            log.info("Iniciando persistencia da conta de credito para o parceiro: {}", partnerId)

            val document = creditAccountModel.toDocument()
            val savedDocument = repo.save(document)

            savedDocument.toModel().also {
                log.info("Conta de credito salva com sucesso no MongoDB para o parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {
            log.error("Erro critico de infraestrutura ao salvar dados no MongoDB para o parceiro: ${creditAccountModel.partnerId}", ex)
            throw DatabaseOperationException("Falha operacional ao salvar dados da conta", ex)
        }

    override fun deleteById(partnerId: String) =
        try {
            log.info("Iniciando delecao da conta de parceiro: {}", partnerId)

            repo.deleteById(partnerId).also {
                log.info("Conta de credito deletada com sucesso para parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {
            log.error("Erro critico de infraestrutura ao deletar dados no MongoDB para o parceiro: $partnerId", ex)
            throw DatabaseOperationException("Falha operacional ao deletar dados da conta", ex)
        }
}
