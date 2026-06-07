package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountAlreadyExistsException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.CreditAccountModel
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PartnerAccountUseCase(
    private val creditAccountRepositoryPort: CreditAccountRepositoryPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createAccountPartner(partnerId: String): CreditAccountModel =
        try {
            log.info("Iniciando processo de cadastro de conta por parceiro: {}", partnerId)

            val existingAccount = creditAccountRepositoryPort.findNewPartnerId(partnerId)

            if (existingAccount.isPresent) {
                log.warn("Tentativa de cadastro duplicado rejeitada para o parceiro: {}", partnerId)
                throw PartnerAccountAlreadyExistsException("O parceiro $partnerId ja possui uma conta ativa no sistema.")
            }

            val newAccount = CreditAccountModel(
                id = null,
                partnerId = partnerId,
                balance = BigDecimal.ZERO
            )

            creditAccountRepositoryPort.save(newAccount).also {
                log.info("Nova conta de parceiro B2B persistido com sucesso: {}", partnerId)
            }

        } catch (ex: Exception) {
            when (ex) {
                is DatabaseOperationException, is PartnerAccountAlreadyExistsException -> throw ex
                else -> {
                    log.error("Erro inesperado ao validar dados de cadastro para o parceiro: $partnerId", ex)
                    throw RuntimeException("Erro interno ao processar a validacao de cadastro", ex)
                }
            }
        }

    @CacheEvict(value = ["getBalance"], key = "#partnerId")
    fun deleteAccountPartner(partnerId: String) =
        try {
            log.info("Iniciando processo de exclusao para o parceiro: {}", partnerId)

            val account = creditAccountRepositoryPort.findByPartnerId(partnerId)

            creditAccountRepositoryPort.deleteById(account.id!!)
            log.info("Conta de parceiro {} removida.", partnerId)

        } catch (ex: Exception) {
            when (ex) {
                is PartnerAccountNotFoundException -> throw PartnerAccountNotFoundException("Conta de parceiro já excluida, parceiro: $partnerId")
                is DatabaseOperationException -> throw ex
                else -> {
                    log.error("Erro inesperado e desconhecido ao excluir o parceiro: $partnerId", ex)
                    throw ex
                }
            }
        }
}
