package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.NotificationMessagePort
import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class ReconcilePendingTransactionsUseCaseTest {

    private val transactionRepositoryPort = mockk<TransactionRepositoryPort>(relaxed = true)
    private val notificationPort = mockk<NotificationMessagePort>(relaxed = true)
    private val useCase = ReconcilePendingTransactionsUseCase(transactionRepositoryPort, notificationPort)

    @Test
    fun `deve conciliar transacoes pendentes com sucesso`() {
        val transaction = TransactionDocument(
            id = "tx-pending-1",
            partnerId = "partner-1",
            type = TransactionType.CREDIT,
            amount = BigDecimal("100.00"),
            description = "Teste",
            status = TransactionStatusType.PENDING,
            createdAt = LocalDateTime.now().minusMinutes(10)
        )
        val updatedTransaction = transaction.copy(status = TransactionStatusType.COMPLETED)

        every { transactionRepositoryPort.findByStatusAndCreatedAtBefore(any(), any()) } returns listOf(transaction)
        every { transactionRepositoryPort.save(any()) } returns updatedTransaction

        useCase.execute()

        verify(exactly = 1) { notificationPort.sendTransactionNotification(any()) }
        verify(exactly = 1) { transactionRepositoryPort.save(withArg { assertEquals(TransactionStatusType.COMPLETED, it.status) }) }
    }

    @Test
    fun `deve encerrar execucao se nao houver nenhuma transacao pendente`() {
        every { transactionRepositoryPort.findByStatusAndCreatedAtBefore(any(), any()) } returns emptyList()

        useCase.execute()

        verify(exactly = 0) { notificationPort.sendTransactionNotification(any()) }
        verify(exactly = 0) { transactionRepositoryPort.save(any()) }
    }

    @Test
    fun `deve continuar processamento caso ocorra um erro em uma transacao especifica`() {
        val tx1 = TransactionDocument("tx-1", "partner-1", TransactionType.CREDIT, BigDecimal.ONE, "T1", TransactionStatusType.PENDING, LocalDateTime.now().minusMinutes(10))
        val tx2 = TransactionDocument("tx-2", "partner-2", TransactionType.DEBIT, BigDecimal.TEN, "T2", TransactionStatusType.PENDING, LocalDateTime.now().minusMinutes(10))

        every { transactionRepositoryPort.findByStatusAndCreatedAtBefore(any(), any()) } returns listOf(tx1, tx2)
        every { notificationPort.sendTransactionNotification(match { it.transactionId == "tx-1" }) } throws RuntimeException("Erro na fila")
        every { transactionRepositoryPort.save(any()) } returns tx2.copy(status = TransactionStatusType.COMPLETED)

        useCase.execute()

        verify(exactly = 1) { notificationPort.sendTransactionNotification(match { it.transactionId == "tx-1" }) }
        verify(exactly = 1) { notificationPort.sendTransactionNotification(match { it.transactionId == "tx-2" }) }
        verify(exactly = 0) { transactionRepositoryPort.save(match { it.id == "tx-1" }) }
        verify(exactly = 1) { transactionRepositoryPort.save(match { it.id == "tx-2" }) }
    }

    @Test
    fun `deve interromper execucao e lancar DatabaseOperationException quando falhar a busca inicial`() {
        every { transactionRepositoryPort.findByStatusAndCreatedAtBefore(any(), any()) } throws DatabaseOperationException("Falha no banco")

        assertThrows<DatabaseOperationException> {
            useCase.execute()
        }

        verify(exactly = 0) { notificationPort.sendTransactionNotification(any()) }
    }
}
