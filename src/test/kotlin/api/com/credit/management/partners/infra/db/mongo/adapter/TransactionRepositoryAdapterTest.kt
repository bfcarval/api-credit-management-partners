package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.enums.TransactionStatusType
import api.com.credit.management.partners.domain.model.enums.TransactionType
import api.com.credit.management.partners.infra.db.mongo.document.TransactionDocument
import api.com.credit.management.partners.infra.db.mongo.repository.TransactionMongoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class TransactionRepositoryAdapterTest {

    private val transactionMongoRepository = mockk<TransactionMongoRepository>(relaxed = true)
    private val adapter = TransactionRepositoryAdapter(transactionMongoRepository)

    private val partnerId = "partner-b2b-99"
    private val now = LocalDateTime.now()

    @Test
    fun `deve salvar transacao com sucesso`() {
        val transaction = TransactionDocument(id = "tx-123", partnerId = partnerId, type = TransactionType.CREDIT, amount = BigDecimal("100.00"), description = "Teste", status = TransactionStatusType.PENDING, createdAt = now)

        every { transactionMongoRepository.save(transaction) } returns transaction

        val result = adapter.save(transaction)

        assertNotNull(result)
        assertEquals(transaction.id, result.id)
        verify(exactly = 1) { transactionMongoRepository.save(transaction) }
    }

    @Test
    fun `deve lancar DatabaseOperationException ao falhar salvamento no banco`() {
        val transaction = TransactionDocument(id = "tx-123", partnerId = partnerId, type = TransactionType.CREDIT, amount = BigDecimal("100.00"), description = "Teste", status = TransactionStatusType.PENDING, createdAt = now)

        every { transactionMongoRepository.save(transaction) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.save(transaction)
        }
    }

    @Test
    fun `deve buscar transacoes por partnerId com sucesso`() {
        val transaction = TransactionDocument(id = "tx-123", partnerId = partnerId, type = TransactionType.CREDIT, amount = BigDecimal("100.00"), description = "Teste", status = TransactionStatusType.COMPLETED, createdAt = now)

        every { transactionMongoRepository.findByPartnerId(partnerId) } returns listOf(transaction)

        val result = adapter.findByPartnerId(partnerId)

        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
        verify(exactly = 1) { transactionMongoRepository.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar PartnerAccountNotFoundException quando a busca por partnerId retornar vazia`() {
        every { transactionMongoRepository.findByPartnerId(partnerId) } returns emptyList()

        assertThrows<PartnerAccountNotFoundException> {
            adapter.findByPartnerId(partnerId)
        }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando ocorrer erro de banco ao buscar por partnerId`() {
        every { transactionMongoRepository.findByPartnerId(partnerId) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.findByPartnerId(partnerId)
        }
    }

    @Test
    fun `deve buscar por status e data com sucesso para reprocessamento`() {
        val transaction = TransactionDocument(id = "tx-123", partnerId = partnerId, type = TransactionType.CREDIT, amount = BigDecimal("100.00"), description = "Teste", status = TransactionStatusType.PENDING, createdAt = now.minusMinutes(10))
        val status = "PENDING"
        val threshold = now.minusMinutes(5)

        every { transactionMongoRepository.findByStatusAndCreatedAtBefore(status, threshold) } returns listOf(transaction)

        val result = adapter.findByStatusAndCreatedAtBefore(status, threshold)

        assertFalse(result.isEmpty())
        assertEquals(1, result.size)
        verify(exactly = 1) { transactionMongoRepository.findByStatusAndCreatedAtBefore(status, threshold) }
    }

    @Test
    fun `deve lancar RuntimeException quando ocorrer erro de banco na busca por status e data`() {
        val status = "PENDING"
        val threshold = now.minusMinutes(5)

        every { transactionMongoRepository.findByStatusAndCreatedAtBefore(status, threshold) } throws RuntimeException("Erro Mongo")

        assertThrows<RuntimeException> {
            adapter.findByStatusAndCreatedAtBefore(status, threshold)
        }
    }
}
