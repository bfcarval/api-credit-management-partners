package api.com.credit.management.partners.infra.db.mongo.adapter

import api.com.credit.management.partners.domain.exception.DatabaseOperationException
import api.com.credit.management.partners.domain.exception.PartnerAccountNotFoundException
import api.com.credit.management.partners.domain.model.CreditAccountModel
import api.com.credit.management.partners.infra.db.mongo.document.CreditAccountDocument
import api.com.credit.management.partners.infra.db.mongo.repository.CreditAccountMongoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
class CreditAccountRepositoryAdapterTest {

    private val repo = mockk<CreditAccountMongoRepository>(relaxed = true)
    private val adapter = CreditAccountRepositoryAdapter(repo)

    private val partnerId = "partner-b2b-99"
    private val now = LocalDateTime.now()

    @Test
    fun `deve buscar conta por partnerId com sucesso`() {
        val document = CreditAccountDocument(id = "doc-123", partnerId = partnerId, balance = BigDecimal("100.00"), version = 0L, updatedAt = now)

        every { repo.findByPartnerId(partnerId) } returns Optional.of(document)

        val result = adapter.findByPartnerId(partnerId)

        assertNotNull(result)
        assertEquals(document.id, result.id)
        assertEquals(document.partnerId, result.partnerId)
        verify(exactly = 1) { repo.findByPartnerId(partnerId) }
    }

    @Test
    fun `deve lancar PartnerAccountNotFoundException ao buscar conta inexistente`() {
        every { repo.findByPartnerId(partnerId) } returns Optional.empty()

        assertThrows<PartnerAccountNotFoundException> {
            adapter.findByPartnerId(partnerId)
        }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando ocorrer erro de banco ao buscar conta`() {
        every { repo.findByPartnerId(partnerId) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.findByPartnerId(partnerId)
        }
    }

    @Test
    fun `deve retornar Optional com modelo quando findNewPartnerId localizar a conta`() {
        val document = CreditAccountDocument(id = "doc-123", partnerId = partnerId, balance = BigDecimal("100.00"), version = 0L, updatedAt = now)

        every { repo.findByPartnerId(partnerId) } returns Optional.of(document)

        val result = adapter.findNewPartnerId(partnerId)

        assertTrue(result.isPresent)
        assertEquals(partnerId, result.get().partnerId)
    }

    @Test
    fun `deve retornar Optional vazio quando findNewPartnerId nao localizar a conta`() {
        every { repo.findByPartnerId(partnerId) } returns Optional.empty()

        val result = adapter.findNewPartnerId(partnerId)

        assertFalse(result.isPresent)
    }

    @Test
    fun `deve lancar DatabaseOperationException quando ocorrer erro de banco no findNewPartnerId`() {
        every { repo.findByPartnerId(partnerId) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.findNewPartnerId(partnerId)
        }
    }

    @Test
    fun `deve salvar conta com sucesso`() {
        val model = CreditAccountModel(id = "mod-123", partnerId = partnerId, balance = BigDecimal("50.00"), version = 1L, updatedAt = now)
        val document = CreditAccountDocument(id = "mod-123", partnerId = partnerId, balance = BigDecimal("50.00"), version = 1L, updatedAt = now)

        every { repo.save(any()) } returns document

        val result = adapter.save(model)

        assertNotNull(result)
        assertEquals(model.id, result.id)
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando ocorrer erro ao salvar conta`() {
        val model = CreditAccountModel(id = "mod-123", partnerId = partnerId, balance = BigDecimal("50.00"), version = 1L, updatedAt = now)

        every { repo.save(any()) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.save(model)
        }
    }

    @Test
    fun `deve deletar conta por ID com sucesso`() {
        every { repo.deleteById(partnerId) } returns Unit

        adapter.deleteById(partnerId)

        verify(exactly = 1) { repo.deleteById(partnerId) }
    }

    @Test
    fun `deve lancar DatabaseOperationException quando ocorrer erro ao deletar conta`() {
        every { repo.deleteById(partnerId) } throws RuntimeException("Erro Mongo")

        assertThrows<DatabaseOperationException> {
            adapter.deleteById(partnerId)
        }
    }
}
