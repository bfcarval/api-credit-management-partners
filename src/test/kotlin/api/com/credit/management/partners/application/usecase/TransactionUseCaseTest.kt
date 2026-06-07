package api.com.credit.management.partners.application.usecase

import api.com.credit.management.partners.application.port.output.TransactionRepositoryPort
import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
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
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class TransactionUseCaseTest {

    private val transactionRepositoryPort = mockk<TransactionRepositoryPort>(relaxed = true)
    private val useCase = TransactionUseCase(transactionRepositoryPort)

    private val partnerId = "partner-b2b-99"

    @Test
    fun `deve retornar o historico de transacoes ordenado por data de forma decrescente`() {
        val baseTime = LocalDateTime.now()
        val txAntiga = TransactionDocument("tx-1", partnerId, TransactionType.CREDIT, BigDecimal.TEN, "Antiga", TransactionStatusType.COMPLETED, baseTime.minusDays(2))
        val txRecente = TransactionDocument("tx-2", partnerId, TransactionType.DEBIT, BigDecimal.ONE, "Recente", TransactionStatusType.COMPLETED, baseTime)

        every { transactionRepositoryPort.findByPartnerId(partnerId) } returns listOf(txAntiga, txRecente)

        val result = useCase.getTransactionHistory(partnerId)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("tx-2", result[0].id)
        assertEquals("tx-1", result[1].id)

        verify(exactly = 1) { transactionRepositoryPort.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar PartnerAccountNotFoundException quando o parceiro nao tiver historico`() {
        every { transactionRepositoryPort.findByPartnerId(partnerId) } throws PartnerAccountNotFoundException("Historico nao encontrado")

        assertThrows<PartnerAccountNotFoundException> {
            useCase.getTransactionHistory(partnerId)
        }

        verify(exactly = 1) { transactionRepositoryPort.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando falhar a busca no banco de dados`() {
        every { transactionRepositoryPort.findByPartnerId(partnerId) } throws DatabaseOperationException("Falha no MongoDB")

        assertThrows<DatabaseOperationException> {
            useCase.getTransactionHistory(partnerId)
        }

        verify(exactly = 1) { transactionRepositoryPort.findByPartnerId(partnerId) }
    }
}
