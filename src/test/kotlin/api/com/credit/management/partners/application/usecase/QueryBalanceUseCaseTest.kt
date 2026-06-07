package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.domain.model.vo.MoneyVO
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class QueryBalanceUseCaseTest {

    private val accountRepository = mockk<CreditAccountRepositoryPort>(relaxed = true)
    private val useCase = QueryBalanceUseCase(accountRepository)

    private val partnerId = "partner-b2b-99"

    @Test
    fun `deve retornar o saldo do parceiro com sucesso`() {
        val account = CreditAccountModel(id = "acc-1", partnerId = partnerId, balance = BigDecimal("250.50"))

        every { accountRepository.findByPartnerId(partnerId) } returns account

        val result = useCase.getBalance(partnerId)

        assertNotNull(result)
        assertEquals(BigDecimal("250.50"), result.amount)

        verify(exactly = 1) { accountRepository.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar PartnerAccountNotFoundException quando o parceiro nao existir`() {
        every { accountRepository.findByPartnerId(partnerId) } throws PartnerAccountNotFoundException("Conta nao encontrada")

        assertThrows<PartnerAccountNotFoundException> {
            useCase.getBalance(partnerId)
        }

        verify(exactly = 1) { accountRepository.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando falhar a comunicacao com o banco`() {
        every { accountRepository.findByPartnerId(partnerId) } throws DatabaseOperationException("Falha no MongoDB")

        assertThrows<DatabaseOperationException> {
            useCase.getBalance(partnerId)
        }

        verify(exactly = 1) { accountRepository.findByPartnerId(partnerId) }
    }
}
