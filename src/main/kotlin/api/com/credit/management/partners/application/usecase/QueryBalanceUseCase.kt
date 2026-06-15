package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.domain.mapper.toMoneyVO
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QueryBalanceUseCase(
    private val accountRepository: CreditAccountRepositoryPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Cacheable(value = ["getBalance"], key = "#partnerId")
    @Transactional
    fun getBalance(partnerId: String): MoneyVO =
        try {
            log.info("Iniciando consulta de saldo para o parceiro: {}", partnerId)
            val account = accountRepository.findByPartnerId(partnerId)

            account.toMoneyVO().also {
                log.info("Saldo localizado com sucesso para o parceiro: {}", partnerId)
            }

        } catch (ex: Exception) {
            when (ex) {
                is PartnerAccountNotFoundException -> throw ex
                is DatabaseOperationException -> throw ex
                else -> {
                    log.error("Erro inesperado e desconhecido ao processar consulta de saldo para o parceiro: $partnerId", ex)
                    throw RuntimeException("Erro interno ao processar a consulta de saldo", ex)
                }
            }
        }
}
