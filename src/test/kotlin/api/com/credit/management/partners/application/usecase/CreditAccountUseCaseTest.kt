package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.CreditAccountRepositoryPort
import api.com.credit.management.partners.application.port.output.NotificationMessagePort
import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.InsufficientBalanceException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class CreditAccountUseCaseTest {

    private val accountRepository = mockk<CreditAccountRepositoryPort>(relaxed = true)
    private val transactionRepositoryPort = mockk<TransactionRepositoryPort>(relaxed = true)
    private val notificationPort = mockk<NotificationMessagePort>(relaxed = true)

    private val useCase = CreditAccountUseCase(accountRepository, transactionRepositoryPort, notificationPort)

    private val partnerId = "partner-b2b-99"
    private val description = "Operacao de teste"

    @Test
    fun `deve adicionar creditos com sucesso e disparar notificacao`() {
        val amount = BigDecimal("500.00")
        val account = CreditAccountModel(id = "acc-1", partnerId = partnerId, balance = BigDecimal("100.00"))

        val pendingTx = TransactionDocument(
            id = "tx-1",
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = amount,
            description = description,
            status = TransactionStatusType.PENDING,
            createdAt = LocalDateTime.now()
        )
        val completedTx = pendingTx.copy(status = TransactionStatusType.COMPLETED, completedAt = LocalDateTime.now())

        every { transactionRepositoryPort.save(any()) } returns pendingTx andThen completedTx
        every { accountRepository.findByPartnerId(partnerId) } returns account
        every { accountRepository.save(any()) } returns account

        val result = useCase.addCredits(partnerId, amount, description)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(TransactionStatusType.COMPLETED, result.status)
        Assertions.assertEquals(amount, result.amount)

        verify(exactly = 1) { accountRepository.save(any()) }
        verify(exactly = 1) { notificationPort.sendTransactionNotification(any()) }
    }

    @Test
    fun `deve marcar transacao como FAILED quando adicao falhar por conta nao encontrada`() {
        val amount = BigDecimal("500.00")
        val pendingTx = TransactionDocument(
            id = "tx-1",
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = amount,
            description = description,
            status = TransactionStatusType.PENDING,
            createdAt = LocalDateTime.now()
        )

        every { transactionRepositoryPort.save(any()) } returns pendingTx
        every { accountRepository.findByPartnerId(partnerId) } throws PartnerAccountNotFoundException("Conta ausente")

        assertThrows<PartnerAccountNotFoundException> {
            useCase.addCredits(partnerId, amount, description)
        }

        verify(exactly = 1) {
            transactionRepositoryPort.save(withArg {
                Assertions.assertEquals(TransactionStatusType.FAILED, it.status)
            })
        }
    }

    @Test
    fun `deve debitar creditos com sucesso quando possuir saldo suficiente`() {
        val amount = BigDecimal("30.00")
        val account = CreditAccountModel(id = "acc-1", partnerId = partnerId, balance = BigDecimal("100.00"))

        val pendingTx = TransactionDocument(
            id = "tx-2",
            partnerId = partnerId,
            type = TransactionType.DEBIT,
            amount = amount,
            description = description,
            status = TransactionStatusType.PENDING,
            createdAt = LocalDateTime.now()
        )
        val completedTx = pendingTx.copy(status = TransactionStatusType.COMPLETED, completedAt = LocalDateTime.now())

        every { transactionRepositoryPort.save(any()) } returns pendingTx andThen completedTx
        every { accountRepository.findByPartnerId(partnerId) } returns account
        every { accountRepository.save(any()) } returns account

        val result = useCase.debitCredits(partnerId, amount, description)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(TransactionStatusType.COMPLETED, result.status)

        verify(exactly = 1) { accountRepository.save(any()) }
        verify(exactly = 1) { notificationPort.sendTransactionNotification(any()) }
    }

    @Test
    fun `deve lancar InsufficientBalanceException e marcar como FAILED se nao houver saldo`() {
        val amount = BigDecimal("200.00")
        val account = CreditAccountModel(id = "acc-1", partnerId = partnerId, balance = BigDecimal("50.00"))
        val pendingTx = TransactionDocument(
            id = "tx-2",
            partnerId = partnerId,
            type = TransactionType.DEBIT,
            amount = amount,
            description = description,
            status = TransactionStatusType.PENDING,
            createdAt = LocalDateTime.now()
        )

        every { transactionRepositoryPort.save(any()) } returns pendingTx
        every { accountRepository.findByPartnerId(partnerId) } returns account

        assertThrows<InsufficientBalanceException> {
            useCase.debitCredits(partnerId, amount, description)
        }

        verify(exactly = 1) {
            transactionRepositoryPort.save(withArg {
                Assertions.assertEquals(TransactionStatusType.FAILED, it.status)
            })
        }
    }
}