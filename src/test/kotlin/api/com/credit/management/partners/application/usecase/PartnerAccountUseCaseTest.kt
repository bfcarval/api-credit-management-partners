package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountAlreadyExistsException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.CreditAccountModel
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
import java.util.Optional

@ExtendWith(MockKExtension::class)
class PartnerAccountUseCaseTest {

    private val creditAccountRepositoryPort = mockk<CreditAccountRepositoryPort>(relaxed = true)
    private val useCase = PartnerAccountUseCase(creditAccountRepositoryPort)

    private val partnerId = "partner-b2b-99"

    @Test
    fun `deve cadastrar parceiro com sucesso quando nao existir conta ativa`() {
        val newAccount = CreditAccountModel(id = "acc-99", partnerId = partnerId, balance = BigDecimal.ZERO)

        every { creditAccountRepositoryPort.findNewPartnerId(partnerId) } returns Optional.empty()
        every { creditAccountRepositoryPort.save(any()) } returns newAccount

        val result = useCase.createAccountPartner(partnerId)

        assertNotNull(result)
        assertEquals(partnerId, result.partnerId)
        assertEquals(BigDecimal.ZERO, result.balance)

        verify(exactly = 1) { creditAccountRepositoryPort.save(any()) }
    }

    @Test
    fun `deve lancar PartnerAccountAlreadyExistsException ao tentar cadastrar parceiro duplicado`() {
        val existingAccount = CreditAccountModel(id = "acc-1", partnerId = partnerId, balance = BigDecimal.TEN)

        every { creditAccountRepositoryPort.findNewPartnerId(partnerId) } returns Optional.of(existingAccount)

        assertThrows<PartnerAccountAlreadyExistsException> {
            useCase.createAccountPartner(partnerId)
        }

        verify(exactly = 0) { creditAccountRepositoryPort.save(any()) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando falhar ao cadastrar parceiro no banco`() {
        every { creditAccountRepositoryPort.findNewPartnerId(partnerId) } returns Optional.empty()
        every { creditAccountRepositoryPort.save(any()) } throws DatabaseOperationException("Erro de persistência")

        assertThrows<DatabaseOperationException> {
            useCase.createAccountPartner(partnerId)
        }
    }

    @Test
    fun `deve excluir parceiro com sucesso do banco de dados`() {
        val account = CreditAccountModel(id = "acc-88", partnerId = partnerId, balance = BigDecimal.ZERO)

        every { creditAccountRepositoryPort.findByPartnerId(partnerId) } returns account
        every { creditAccountRepositoryPort.deleteById(account.id!!) } returns Unit

        useCase.deleteAccountPartner(partnerId)

        verify(exactly = 1) { creditAccountRepositoryPort.deleteById(account.id!!) }
    }

    @Test
    fun `deve lancar PartnerAccountNotFoundException ao tentar excluir parceiro inexistente`() {
        every { creditAccountRepositoryPort.findByPartnerId(partnerId) } throws PartnerAccountNotFoundException("Conta ausente")

        assertThrows<PartnerAccountNotFoundException> {
            useCase.deleteAccountPartner(partnerId)
        }

        verify(exactly = 0) { creditAccountRepositoryPort.deleteById(any()) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando falhar ao excluir parceiro no banco`() {
        val account = CreditAccountModel(id = "acc-88", partnerId = partnerId, balance = BigDecimal.ZERO)

        every { creditAccountRepositoryPort.findByPartnerId(partnerId) } returns account
        every { creditAccountRepositoryPort.deleteById(account.id!!) } throws DatabaseOperationException("Erro de banco")

        assertThrows<DatabaseOperationException> {
            useCase.deleteAccountPartner(partnerId)
        }
    }
}
